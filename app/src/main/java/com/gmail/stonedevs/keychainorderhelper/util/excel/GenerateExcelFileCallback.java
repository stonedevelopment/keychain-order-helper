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

package com.gmail.stonedevs.keychainorderhelper.util.excel;

import android.net.Uri;

/**
 * Helper interface used when an intent is prepared and ready for action. Currently, just a send
 * action intent used with emailing the generated Excel file.
 */
public interface GenerateExcelFileCallback {

  void onFileGenerationSuccess(Uri uri);

  void onFileGenerationFail();
}
