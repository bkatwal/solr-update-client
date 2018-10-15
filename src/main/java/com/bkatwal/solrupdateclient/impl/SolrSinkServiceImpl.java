package com.bkatwal.solrupdateclient.impl;

import static org.apache.solr.common.SolrException.ErrorCode.SERVER_ERROR;

import com.bkatwal.solrupdateclient.api.SolrSinkService;
import com.bkatwal.solrupdateclient.util.SolrAtomicUpdateOperations;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author "Bikas Katwal" 06/09/18
 */
public class SolrSinkServiceImpl implements SolrSinkService {

  private static final long serialVersionUID = -3642253074708905878L;
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrSinkServiceImpl.class);
  private SolrClient solrClient;

  private int commitWithinMs;

  private static final int ZK_CLIENT_TIMEOUT = 20000;

  public SolrSinkServiceImpl(final String solrZkHostPort, final int commitWithinMs) {
    List<String> zkHosts = Arrays.asList(solrZkHostPort.split(","));

    CloudSolrClient.Builder builder = new CloudSolrClient.Builder(zkHosts, Optional.empty());
    solrClient = builder.build();
    ((CloudSolrClient) solrClient).setZkClientTimeout(ZK_CLIENT_TIMEOUT);
    ((CloudSolrClient) solrClient).setZkConnectTimeout(ZK_CLIENT_TIMEOUT);
    this.commitWithinMs = commitWithinMs;
  }

  public SolrSinkServiceImpl(final SolrClient solrClient, final int commitWithinMs) {
    this.solrClient = solrClient;
    this.commitWithinMs = commitWithinMs;
  }

  @Override
  public UpdateResponse deleteById(final String collection, final String id) {

    try {
      return solrClient.deleteById(collection, id, commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Failed to delete record with id: ".concat(id), e);
    }
  }

  @Override
  public UpdateResponse deleteByIds(String collection, List<String> ids) {
    try {
      return solrClient.deleteById(collection, ids, commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Failed to delete records: ", e);
    }
  }

  @Override
  public UpdateResponse deleteAll(final String collection) {
    try {
      return solrClient.deleteByQuery(collection, "*:*", commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Failed to delete all records: ", e);
    }
  }

  @Override
  public UpdateResponse updateSingleDoc(final String collection, final Map<String, Object> record) {
    try {
      return solrClient.add(collection, convertToSolrInputDocument(record), commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Failed to update single record: ", e);
    }
  }

  @Override
  public <T> UpdateResponse updateSingleDoc(final String collection, final T record) {

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map =
        objectMapper.convertValue(record, new TypeReference<Map<String, Object>>() {
        });
    return updateSingleDoc(collection, map);
  }

  @Override
  public UpdateResponse updateBatchDoc(
      final String collection, final List<Map<String, Object>> records) {

    try {
      List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
      records.forEach(record -> solrInputDocuments.add(convertToSolrInputDocument(record)));
      return solrClient.add(collection, solrInputDocuments, commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Failed to update batch records: ", e);
    }
  }

  @Override
  public <T> UpdateResponse updateBatchDoc(String collection, Collection<T> records) {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, Object>> maps =
        objectMapper.convertValue(records, new TypeReference<List<Map<String, Object>>>() {
        });
    return updateBatchDoc(collection, maps);
  }

  @Override
  public UpdateResponse updateFieldsInDoc(
      final String collection,
      final String id,
      final String field,
      final SolrAtomicUpdateOperations solrAtomicUpdateOperations,
      final Object newVal) {

    try {
      SolrInputDocument sdoc = new SolrInputDocument();
      sdoc.addField("id", id);
      Map<String, Object> fieldModifier = new HashMap<>(1);
      fieldModifier.put(solrAtomicUpdateOperations.name().toLowerCase(), newVal);
      sdoc.addField(field, fieldModifier);

      return solrClient.add(collection, sdoc, commitWithinMs);
    } catch (Exception e) {
      throw new SolrException(SERVER_ERROR, "Atomic update failed: ", e);
    }
  }

  @Override
  public void closeSolrClient() {
    try {
      solrClient.close();
    } catch (IOException e) {
      LOGGER.error("could not close solr client! {}", e);
    }
  }

  // BKTODO validate unrecognized type in value
  public SolrInputDocument convertToSolrInputDocument(Map<String, Object> record) {
    SolrInputDocument doc = new SolrInputDocument();
    record.forEach(
        (key, val) -> {
          if (val != null) {
            if ("_childDocuments_".equalsIgnoreCase(key)) {
              doc.addChildDocuments(getChildDocuments(val));
            }
            // adding shitty logic as solr doesn't support BigDecimal
            if (val instanceof BigDecimal) {
              val = ((BigDecimal) val).doubleValue();
            }
            doc.setField(key, val);
          }
        });
    return doc;
  }

  private Collection<SolrInputDocument> getChildDocuments(Object childDocuments) {

    List<Map<String, Object>> childDocsList = (List<Map<String, Object>>) childDocuments;

    List<SolrInputDocument> solrInputDocuments = new ArrayList<>(childDocsList.size());
    for (Map<String, Object> record : childDocsList) {
      SolrInputDocument solrInputDocument = new SolrInputDocument();
      record.forEach(
          (key, val) -> {
            if (val != null) {
              // adding shitty logic as solr doesn't support BigDecimal
              if (val instanceof BigDecimal) {
                val = ((BigDecimal) val).doubleValue();
              }
              solrInputDocument.setField(key, val);
            }
          });
      solrInputDocuments.add(solrInputDocument);
    }
    return solrInputDocuments;
  }
}
