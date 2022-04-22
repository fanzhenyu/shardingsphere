/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shadow.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Properties;

/**
 * Shadow algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(ShadowAlgorithm.class);
    }
    
    /**
     * Create new instance of shadow algorithm.
     * 
     * @param shadowAlgorithmConfig shadow algorithm configuration
     * @return new instance of shadow algorithm
     */
    public static ShadowAlgorithm newInstance(final ShardingSphereAlgorithmConfiguration shadowAlgorithmConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(shadowAlgorithmConfig, ShadowAlgorithm.class);
    }
    
    /**
     * Judge whether contains shadow algorithm.
     *
     * @param shadowAlgorithmType shadow algorithm type
     * @return contains shadow algorithm or not
     */
    public static boolean contains(final String shadowAlgorithmType) {
        return TypedSPIRegistry.findRegisteredService(ShadowAlgorithm.class, shadowAlgorithmType, new Properties()).isPresent();
    }
}