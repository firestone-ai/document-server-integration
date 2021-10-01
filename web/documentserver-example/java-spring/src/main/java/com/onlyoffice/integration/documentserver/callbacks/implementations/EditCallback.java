/**
 *
 * (c) Copyright Ascensio System SIA 2021
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
 *
 */

package com.onlyoffice.integration.documentserver.callbacks.implementations;

import com.onlyoffice.integration.documentserver.callbacks.Callback;
import com.onlyoffice.integration.documentserver.callbacks.Status;
import com.onlyoffice.integration.documentserver.managers.callback.CallbackManager;
import com.onlyoffice.integration.dto.Action;
import com.onlyoffice.integration.dto.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EditCallback implements Callback {
    @Autowired
    private CallbackManager callbackManager;
    @Override
    public int handle(Track body, String fileName) {
        int result = 0;
        Action action =  body.getActions().get(0);
        if (action.getType().equals(com.onlyoffice.integration.documentserver.models.enums.Action.edit)) {
            String user =  action.getUserid();
            if (!body.getUsers().contains(user)) {
                String key = body.getKey();
                try {
                    callbackManager.commandRequest("forcesave", key);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 1;
                }
            }
        }
        return result;
    }

    @Override
    public int getStatus() {
        return Status.EDITING.getCode();
    }
}
