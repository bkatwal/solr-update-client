package com.bkatwal.solrupdateclient.api;

/**
 * @author "Bikas Katwal" 15/10/18
 */

import com.bkatwal.solrupdateclient.util.SolrAtomicUpdateOperations;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.UpdateResponse;

/**
 * @author "Bikas Katwal" 06/09/18
 */
public interface SolrSinkService extends Serializable {

  /**
   * @param collection solr collection name
   * @param id solr doc unique key to delete
   * @return solr update response
   */
  UpdateResponse deleteById(final String collection, final String id);

  UpdateResponse deleteByIds(final String collection, final List<String> ids);

  UpdateResponse deleteAll(final String collection);

  /**
   * @param collection solr collection name
   * @param record field value Map of a json doc
   * @return solr update response
   */
  UpdateResponse updateSingleDoc(final String collection, final Map<String, Object> record);

  /**
   * @param collection collection solr collection name
   * @param record POJO class type record to be indexed
   * @param <T> POJO class type
   * @return update response
   */
  <T> UpdateResponse updateSingleDoc(final String collection, final T record);

  /**
   * @param collection solr collection name
   * @param records list of field value Map of multiple json docs
   * @return solr update response
   */
  UpdateResponse updateBatchDoc(final String collection, final List<Map<String, Object>> records);


  /**
   * @param collection collection solr collection name
   * @param records POJO class records to be indexed
   * @param <T> POJO class type
   * @return update response
   */
  <T> UpdateResponse updateBatchDoc(final String collection, final Collection<T> records);

  /**
   * @param collection solr collection name
   * @param id solr doc unique key to update
   * @param field field to update
   * @param solrAtomicUpdateOperations Operation on field, check: https://lucene.apache.org/solr/guide/7_3/updating-parts-of-documents.html
   * for ref
   * @param newVal new value to update
   * @return solr update response
   */
  UpdateResponse updateFieldsInDoc(
      final String collection,
      final String id,
      final String field,
      SolrAtomicUpdateOperations solrAtomicUpdateOperations,
      Object newVal);

  void closeSolrClient();
}