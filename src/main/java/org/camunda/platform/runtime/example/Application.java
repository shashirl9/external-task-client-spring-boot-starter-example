/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.platform.runtime.example;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:9991/engine-rest")
                .asyncResponseTimeout(20000)
                .lockDuration(10000)
                .maxTasks(1)
                .build();

        TopicSubscriptionBuilder subscriptionBuilder = client
                .subscribe("requestRejecter");

        // handle job
        subscriptionBuilder.handler((externalTask, externalTaskService) -> {
            String customerId = externalTask.getVariable("customerId");
            int creditScore = externalTask.getVariable("creditScore");
            Logger.getLogger("requestRejecter")
                    .log(Level.INFO, "Sorry, your loan request for the CustomerId: {0} with Credit-score: {1} has been rejected!", new Object[]{customerId, creditScore});
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("Notfication - Timestamp", new Date());
            externalTaskService.complete(externalTask, variables);

        });

        subscriptionBuilder.open();
    }
}
