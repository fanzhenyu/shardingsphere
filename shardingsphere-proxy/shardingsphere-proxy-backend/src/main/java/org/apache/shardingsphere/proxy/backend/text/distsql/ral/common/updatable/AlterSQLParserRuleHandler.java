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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.util.Collection;
import java.util.Optional;

/**
 * Alter SQL parser rule statement handler.
 */
public final class AlterSQLParserRuleHandler extends UpdatableRALBackendHandler<AlterSQLParserRuleStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        SQLParserRuleConfiguration currentConfig = findCurrentConfiguration();
        SQLParserRuleConfiguration toBeAlteredRuleConfig = createSQLParserRuleConfiguration(currentConfig);
        Collection<ShardingSphereRule> globalRules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        globalRules.removeIf(each -> each instanceof SQLParserRule);
        globalRules.add(new SQLParserRule(toBeAlteredRuleConfig));
        persistNewRuleConfigurations(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations());
    }
    
    private SQLParserRuleConfiguration findCurrentConfiguration() {
        Optional<SQLParserRule> rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(rule.isPresent());
        return rule.get().getConfiguration();
    }
    
    private SQLParserRuleConfiguration createSQLParserRuleConfiguration(final SQLParserRuleConfiguration currentConfig) {
        SQLParserRuleConfiguration result = new SQLParserRuleConfiguration();
        result.setSqlCommentParseEnabled(null == getSqlStatement().getSqlCommentParseEnable() ? currentConfig.isSqlCommentParseEnabled() : getSqlStatement().getSqlCommentParseEnable());
        result.setParseTreeCache(
                null == getSqlStatement().getParseTreeCache() ? currentConfig.getParseTreeCache() : createCacheOption(currentConfig.getParseTreeCache(), getSqlStatement().getParseTreeCache()));
        result.setSqlStatementCache(null == getSqlStatement().getSqlStatementCache() ? currentConfig.getSqlStatementCache()
                : createCacheOption(currentConfig.getSqlStatementCache(), getSqlStatement().getSqlStatementCache()));
        return result;
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    private void persistNewRuleConfigurations(final Collection<RuleConfiguration> globalRuleConfigs) {
        Optional<MetaDataPersistService> metaDataPersistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService();
        if (metaDataPersistService.isPresent() && null != metaDataPersistService.get().getGlobalRuleService()) {
            metaDataPersistService.get().getGlobalRuleService().persist(globalRuleConfigs, true);
        }
    }
}
