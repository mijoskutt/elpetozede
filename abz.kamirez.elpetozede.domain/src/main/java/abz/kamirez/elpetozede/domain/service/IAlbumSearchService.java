/*******************************************************************************
 * Copyright 2017 Michael Skutta
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package abz.kamirez.elpetozede.domain.service;

import java.util.List;

import abz.kamirez.elpetozede.domain.material.Album;

public interface IAlbumSearchService extends IProgressReportingTask
{
  public final static String RELEASE_REGULAR = "reguläres Album";
  public final static String RELEASE_LIVE = "Live-Album";
  public final static String RELEASE_COMPILATION = "Compilation/Sampler";
  public final static String RELEASE_BOOTLEG = "Bootleg";

  public List<Album> findAlbums(String interpret, String title, String releaseType) throws SearchServiceException;
}
