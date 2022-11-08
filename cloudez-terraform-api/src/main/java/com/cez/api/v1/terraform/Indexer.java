package com.cez.api.v1.terraform;

import javax.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class Indexer
{

  private EntityManager entityManager;

  private static final int THREAD_NUMBER = 4;

  public Indexer(EntityManager entityManager)
  {
    this.entityManager = entityManager;
  }

  public void indexPersistedData(String indexClassName)
  {

    try
    {
      SearchSession searchSession = Search.session(entityManager);

      Class<?> classToIndex = Class.forName(indexClassName);
      MassIndexer indexer = searchSession.massIndexer(classToIndex).threadsToLoadObjects(THREAD_NUMBER);

      indexer.startAndWait();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}
