package com.bkatwal.solrupdateclient;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.bkatwal.solrupdateclient.api.SolrSinkService;
import com.bkatwal.solrupdateclient.impl.SolrSinkServiceImpl;
import com.bkatwal.solrupdateclient.util.SolrAtomicUpdateOperations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author "Bikas Katwal" 06/09/18
 */
public class SolrSinkServiceTest {

  @Mock
  private SolrClient solrClientMock;

  private SolrSinkService solrSinkService;

  private String collectionName = "testCollection";

  private String keyWord = "keyword";

  @Before
  public void setup() {
    try {
      MockitoAnnotations.initMocks(this);
      when(solrClientMock.deleteById(anyString(), anyString(), anyInt())).thenReturn(null);
      when(solrClientMock.deleteById(anyString(), anyListOf(String.class), anyInt()))
          .thenReturn(null);
      when(solrClientMock.deleteByQuery(anyString(), anyString(), anyInt())).thenReturn(null);

      when(solrClientMock.add(anyString(), any(SolrInputDocument.class), anyInt()))
          .thenReturn(null);
      when(solrClientMock.add(anyString(), anyListOf(SolrInputDocument.class), anyInt()))
          .thenReturn(null);
      solrSinkService = new SolrSinkServiceImpl(solrClientMock, 1);
    } catch (Exception e) {
      throw new SolrException(ErrorCode.SERVER_ERROR, e);
    }
  }

  @Test
  public void updateSingleDocTest() {

    Map<String, Object> record = new HashMap<>();
    record.put(keyWord, "lemon");
    record.put("id", "111");
    record.put("productsCount", "1000");
    record.put("userHits", "99");
    record.put("categor", Arrays.asList("fruits", "sweet"));

    List<Map<String, Object>> childRecordsList = new ArrayList<>();
    Map<String, Object> childRecord = new HashMap<>();
    childRecord.put("keywordChild", "milk powder");
    childRecord.put("id", "111_1");
    childRecordsList.add(childRecord);
    record.put("_childDocuments_", childRecordsList);

    solrSinkService.updateSingleDoc(collectionName, record);
  }

  @Test
  public void updateBatchDocsTest() {

    List<Map<String, Object>> recordsList = new ArrayList<>();
    Map<String, Object> record = new HashMap<>();
    record.put(keyWord, "milk");
    record.put("id", "1234");
    record.put("someThing", Arrays.asList("a", "b", "c"));
    recordsList.add(record);

    Map<String, Object> record2 = new HashMap<>();
    record2.put(keyWord, "olives");
    record2.put("id", "1235");
    record2.put("test", Arrays.asList("a", "b", "c"));
    recordsList.add(record2);

    solrSinkService.updateBatchDoc(collectionName, recordsList);
  }

  @Test
  public void updateFieldsInDocTest() {

    solrSinkService.updateFieldsInDoc(
        collectionName, "1", "userHits", SolrAtomicUpdateOperations.INC, 10);
  }

  @Test
  public void deleteTest() {

    solrSinkService.deleteById(collectionName, "1");

    solrSinkService.deleteByIds(collectionName, Arrays.asList("2", "3"));

    solrSinkService.deleteAll(collectionName);
  }
}
