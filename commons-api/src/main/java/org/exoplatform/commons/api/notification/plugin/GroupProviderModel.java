/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.plugin;

import java.util.List;

public class GroupProviderModel {
  private String       name;

  private String       resourceBundleKey;

  private List<String> values;

  public GroupProviderModel() {
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the resourceBundleKey
   */
  public String getResourceBundleKey() {
    return resourceBundleKey;
  }

  /**
   * @param resourceBundleKey the resourceBundleKey to set
   */
  public void setResourceBundleKey(String resourceBundleKey) {
    this.resourceBundleKey = resourceBundleKey;
  }

  /**
   * @return the values
   */
  public List<String> getValues() {
    return values;
  }

  /**
   * @param values the values to set
   */
  public void setValues(List<String> values) {
    this.values = values;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GroupProviderModel) {
      return ((GroupProviderModel) obj).getName().equals(this.getName());
    }
    return super.equals(obj);
  }

}
