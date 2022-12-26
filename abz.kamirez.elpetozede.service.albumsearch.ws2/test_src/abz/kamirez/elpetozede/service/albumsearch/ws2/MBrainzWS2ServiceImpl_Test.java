package abz.kamirez.elpetozede.service.albumsearch.ws2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.service.IAlbumSearchService;
import abz.kamirez.elpetozede.domain.service.SearchServiceException;

public class MBrainzWS2ServiceImpl_Test
{
  private MBrainzWS2ServiceImpl m_searcher = new MBrainzWS2ServiceImpl();

  @Test
  public void testListe_der_Alben_nicht_leer_bei_vorhandenen_Eingaben() throws SearchServiceException
  {
    List<Album> result = m_searcher.findAlbums("Thin Lizzy", "Jailbreak", IAlbumSearchService.RELEASE_REGULAR);
    assertTrue(result.size() > 0);
  }

  @Test
  public void test_Live_Album_wird_gefunden() throws SearchServiceException
  {
    List<Album> result = m_searcher.findAlbums("Thin Lizzy", "Live and Dangerous", IAlbumSearchService.RELEASE_LIVE);
    assertTrue(result.size() > 0);
  }

  @Test
  public void testListe_der_Alben_enthaelt_LabelInformationen() throws SearchServiceException
  {
    List<Album> result = m_searcher.findAlbums("Thin Lizzy", "Jailbreak", IAlbumSearchService.RELEASE_REGULAR);

    List<String> labels = new ArrayList<String>();
    boolean mitVertigo = false;

    for (Album tempAlbum : result)
    {
      String tempLabel = tempAlbum.getLabel();
      if (tempLabel.length() > 0)
      {
        if (tempLabel.startsWith("Vertigo"))
        {
          mitVertigo = true;
        }

        if (labels.contains(tempLabel) == false)
        {
          labels.add(tempLabel);
        }
      }
    }

    assertTrue(mitVertigo);
    assertTrue(labels.size() > 1);
  }

  @Test
  public void testVorhandenes_Album_enthaelt_alle_Tracks() throws SearchServiceException
  {
    List<Album> result = m_searcher.findAlbums("Thin Lizzy", "Jailbreak", IAlbumSearchService.RELEASE_REGULAR);

    for (Album album : result)
    {
      assertTrue(album.getTrackCount() > 0);
    }

  }

  @Test
  public void testListe_der_Alben_nicht_leer_bei_vorhandenen_Eingaben_mitSonderzeichen() throws SearchServiceException
  {
    List<Album> result = m_searcher.findAlbums("AC/DC", "Highway to Hell", IAlbumSearchService.RELEASE_REGULAR);
    assertTrue(result.size() > 0);
  }

  @Test
  public void testAC_DC_wird_richtig_aufbereitet()
  {
    String unescapedStr = "AC/DC";
    QueryWrapperWS2 wrapper = new QueryWrapperWS2();
    String escapedStr = wrapper.getEscapedString(unescapedStr);
    assertEquals("AC\\/DC", escapedStr);
  }

}
