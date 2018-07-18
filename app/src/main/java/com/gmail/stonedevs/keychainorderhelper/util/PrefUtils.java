/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.gmail.stonedevs.keychainorderhelper.R;

/**
 * TODO: Add a class header comment!
 */
public class PrefUtils {

  static public SharedPreferences getPrefs(Context c) {
    return PreferenceManager.getDefaultSharedPreferences(c);
  }

  static public String getRepName(Context c) {
    return getPrefs(c).getString(c.getString(R.string.pref_key_rep_name), "");
  }

  static public String getRepTerritory(Context c) {
    return getPrefs(c).getString(c.getString(R.string.pref_key_rep_territory), "");
  }

  static public String getCompanyDivision(Context c) {
    return getPrefs(c).getString(getCompanyDivisionPrefKey(c), "");
  }

  static public boolean isCompanyDivisionDefault(Context c) {
    return getCompanyDivision(c).equals(c.getString(R.string.pref_default_value_company_division));
  }

  static private String getCompanyDivisionPrefKey(Context c) {
    return c.getString(R.string.pref_key_company_division);
  }

  static public boolean isCompanyDivisionPrefKey(Context c, String key) {
    return getCompanyDivisionPrefKey(c).equals(key);
  }
}