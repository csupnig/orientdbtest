/*
 * @author herbert
 * @date 23.03.2006
 * @version $Id$
 */
package com.gentics.api.portalnode.connector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.DatasourceNotAvailableException;
import com.gentics.api.lib.datasource.WriteableDatasource;
import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.resolving.Changeable;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.lib.base.CMSUnavailableException;
import com.gentics.lib.base.NodeIllegalArgumentException;
import com.gentics.lib.content.DatatypeHelper;
import com.gentics.lib.content.GenticsContentFactory;
import com.gentics.lib.datasource.AbstractContentRepositoryStructure;
import com.gentics.lib.datasource.CNDatasource;
import com.gentics.lib.datasource.CNWriteableDatasource;
import com.gentics.lib.datasource.SQLHandle;
import com.gentics.lib.datasource.SimpleHandlePool;
import com.gentics.lib.db.DBHandle;
import com.gentics.lib.log.NodeLogger;
import com.gentics.lib.parser.rule.DefaultRuleTree;
import com.gentics.portalnode.datasources.DatasourceFactoryImpl;
import com.gentics.portalnode.datasources.DatasourceSTRUCT;

/**
 * PortalConnectorFactory can be used to create instances for datasources,
 * RuleTrees and Resolvables.
 * @author herbert
 */
public final class PortalConnectorFactory {
    /**
     * Don't allow any instances of this class...
     */
    private PortalConnectorFactory() {

    }

    /**
     * datasource id used when creating a new SQL handle.
     */
    private static final String DEFAULT_HANDLE_ID = "default";

    /**
     * map of active handles.
     * @see #createDatasource(Map, Map).
     */
    private static Map activeHandles = new HashMap();

    /**
     * map containing all used datasourceFactories ..
     */
    private static Map datasourceFactories = new HashMap();

    private static NodeLogger logger = NodeLogger.getNodeLogger(PortalConnectorFactory.class);

    private static Scheduler scheduler;

    static {
        startScheduler();
    }

    /**
     * Creates a new datasource handle with the specified properties as well as
     * a new Datasource (with default properties).
     * @param handleprops Handle properties used when initializing SQL handle.
     * @return a new initialized Datasource
     * @see Datasource
     * @see #createDatasource(Map, Map)
     */
    public static Datasource createDatasource(Map handleprops) {
        Map dsprops = new HashMap();
        dsprops.put("versioning", "false");
        return createDatasource(handleprops, dsprops);
    }

    /**
     * Creates a new Datasource connection with the specified properties which
     * should point to a Content Repository of Gentics Content.Node. For every
     * different datasource (different handleproperties) a datasource handle
     * will be created and reused with the given pooling settings.<br>
     * Note: the returned instance of {@link Datasource} is NOT thread-safe.
     * <br>
     * 
     * <pre>
     *      Handle Property - Parameters:
     *      type - Type of the datasource. (jndi or jdbc)
     *      For JNDI:
     *      name - The name of the defined JNDI datasource.
     *      
     *      For JDBC:
     *      driverClass = The name of the JDBC class to be used (e.g. com.mysql.jdbc.Driver)
     *      url = The URL which describes the JDBC datasource. (e.g. jdbc:mysql://playground.office:3306/testdb?user=root)
     *      
     *      Optional JDBC Pool Parameters:
     *      maxActive = Controls the maximum number of objects that can be borrowed from the pool at one time.
     *      maxIdle = Controls the maximum number of objects that can sit idle in the pool at any time.
     *      
     *      for more options and more detailed documentation for JDBC Pool options
     *      take a look at the API documentation of
     *      {@link org.apache.commons.pool.impl.GenericObjectPool}
     * </pre>
     * 
     * @param handleprops Handle properties used when initializing SQL handle.
     * @param dsprops Datasource properties, may be an empty map.
     * @return a new and initialized Datasource or null if one of the parameters
     *         where null or the sanity check failed. Datasource may be unusable
     *         (DatasourceNotAvailableException) if invalid properties where
     *         given.
     */
    public static Datasource createDatasource(Map handleprops, Map dsprops) {
        return createGenericDatasource(handleprops, dsprops, CNDatasource.class);

    }

    private static String createUniqueId(Map handleprops, Map dsprops, Class clazz) {
        return new StringBuffer(handleprops.toString()).append('|').append(dsprops.toString())
                .append('|').append(clazz.getName()).toString();
    }

    /**
     * Returns a content object with the given content id.
     * @param contentId The content id of the object which should be returned.
     *        ([objecttype].[object id] e.g. 10002.123)
     * @param datasource Datasource used to load the content object.
     * @return the content object corresponding to the given content id. This
     *         method returns null if datasource is null or contentid is null or
     *         it doesn't exist or the syntax of the contentid is wrong.
     * @throws DatasourceNotAvailableException
     */
    public static Resolvable getContentObject(String contentId, Datasource datasource)
            throws DatasourceNotAvailableException {
        try {
            return GenticsContentFactory.createContentObject(contentId, datasource);
        } catch (CMSUnavailableException e) {
            throw new DatasourceNotAvailableException(e.toString());
        } catch (NodeIllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parse the given expression string into an Expression. You should reuse
     * parsed Expressions whereever possible as well as reuse DatasourceFilters
     * which are created from those expressions.
     * @see Datasource#createDatasourceFilter(Expression)
     * @param rule The Rule string to be parsed as Expression.
     * @return the parsed Expression.
     * @throws ParserException
     */
    public static Expression createExpression(String rule) throws ParserException {
        return ExpressionParser.getInstance().parse(rule);
    }

    /**
     * Creates a new RuleTree with the given rule string. Take care that you
     * reuse parsed RuleTree's wherever possible by replacing the resolver.
     * Parsing strings to RuleTrees is cpu expensive.
     * @param rule The rule string which is used to initialize the RuleTree
     * @return a new and initialized RuleTree to match the given rule string.
     * @throws ParserException when rule has invalid syntax or is null.
     * @see RuleTree
     * @deprecated deprecated, replaced by {@link #createExpression(String)}
     */
    public static com.gentics.api.lib.rule.RuleTree createRuleTree(String rule)
            throws ParserException {

        com.gentics.api.lib.rule.RuleTree ruleTree = new DefaultRuleTree();
        ruleTree.parse(rule);
        return ruleTree;
    }

    /**
     * Creates a new datasource handle with the specified properties as well as
     * a new writeable datasource (with default properties).
     * @param handleProperties Handle properties used when initializing SQL
     *        handle.
     * @return The WriteableDatasource which might be used to read/write content
     *         objects.
     */
    public static WriteableDatasource createWriteableDatasource(Map handleProperties) {
        Map dsprops = new HashMap();
        dsprops.put("versioning", "false");
        return createWriteableDatasource(handleProperties, dsprops);
    }

    /**
     * Creates a new datasource handle with the specified properties as well as
     * a new writeable datasource with the specified properties.
     * @param handleProperties Handle properties used when initializing SQL
     *        handle.
     * @param datasourceProperties Datasource properties, may be an empty map.
     * @return The WriteableDatasource which might be used to read/write content
     *         objects or null if the sanity check failed
     */
    public static WriteableDatasource createWriteableDatasource(Map handleProperties,
            Map datasourceProperties) {
        return (WriteableDatasource) createGenericDatasource(handleProperties,
                datasourceProperties, CNWriteableDatasource.class);
    }

    /**
     * return an instance of the given datasourceclass.
     * @param handleProperties
     * @param datasourceProperties
     * @param clazz
     * @return the new instance. or null if the sanity check failed
     */
    private static Datasource createGenericDatasource(Map handleProperties,
            Map datasourceProperties, Class clazz) {
        SQLHandle handle;
        // check parameters
        if (handleProperties == null || datasourceProperties == null) {
            return null;
        } else {
            boolean sanityCheck = ObjectTransformer.getBoolean(datasourceProperties
                    .get("sanitycheck"), true);
            boolean autoRepair = ObjectTransformer.getBoolean(datasourceProperties
                    .get("autorepair"), true);
            boolean sanityCheck2 = ObjectTransformer.getBoolean(datasourceProperties
                    .get("sanitycheck2"), false);
            boolean autoRepair2 = ObjectTransformer.getBoolean(datasourceProperties
                    .get("autorepair2"), false);

            // lookup existing handles in acvtivehandles map
            synchronized (activeHandles) {
                String handlekey = handleProperties.toString();
                handle = (SQLHandle) activeHandles.get(handlekey);
                // not found, create new handle
                if (handle == null) {
                    handle = new SQLHandle(DEFAULT_HANDLE_ID);
                    handle.init(handleProperties);

                    // set the configured contentrepository table names
                    DBHandle dbHandle = ((SQLHandle) handle).getDBHandle();
                    try {
                        dbHandle.setTableNames(ObjectTransformer.getString(datasourceProperties
                                .get("table.contentstatus"), null), ObjectTransformer
                                .getString(datasourceProperties.get("table.contentobject"),
                                        null), ObjectTransformer.getString(datasourceProperties
                                .get("table.contentattributetype"), null), ObjectTransformer
                                .getString(datasourceProperties.get("table.contentmap"), null),
                                ObjectTransformer.getString(datasourceProperties
                                        .get("table.contentattribute"), null));
                    } catch (DatasourceException e) {
                        logger.error("Error in the customized table configuration", e);
                        return null;
                    }

                    // when the datasource shall be checked for sanity, we do it
                    // now
                    // do not use sanitycheck if sanitycheck2 is enabled
                    if (sanityCheck && !sanityCheck2) {
                        try {
                            if (!DatatypeHelper.checkContentRepository("pc_handle", handle
                                    .getDBHandle(), autoRepair)) {
                                return null;
                            }
                        } catch (CMSUnavailableException e) {
                            logger.error("Error while checking datasource for sanity", e);
                            return null;
                        }
                    }

                    activeHandles.put(handlekey, handle);
                }
            }

            // Find DatasourceFactory
            DatasourceFactoryImpl datasourceFactory = null;
            String id = createUniqueId(handleProperties, datasourceProperties, clazz);
            boolean createdFactory = false;
            // synchronize to prevent multiple creation of same factory
            synchronized (datasourceFactories) {
                datasourceFactory = (DatasourceFactoryImpl) datasourceFactories.get(id);

                // not found, create new factory
                if (datasourceFactory == null) {
                    DatasourceSTRUCT struct = new DatasourceSTRUCT();
                    struct.ID = id;
                    struct.typeID = clazz.getName();
                    struct.ParameterMap = datasourceProperties;
                    datasourceFactory = new DatasourceFactoryImpl(struct);
                    datasourceFactories.put(id, datasourceFactory);
                    datasourceFactory.setHandlePool(new SimpleHandlePool(handle));
                    // we just created a new factory
                    createdFactory = true;
                }
            }

            Datasource ds = datasourceFactory.getInstance();

            // synchronize to prevent multiple parallel sanity checks
            synchronized (datasourceFactory) {
                // only do sanity checks once
                if (createdFactory) {
                    // do sanitycheck2 and autorepair2
                    if (sanityCheck2) {
                        try {
                            AbstractContentRepositoryStructure checkStructure = AbstractContentRepositoryStructure
                                    .getStructure(ds, "pc_handle");
                            boolean check = checkStructure
                                    .checkStructureConsistency(autoRepair2);
                            if (!check) {
                                datasourceFactory.setValid(false);
                            } else {
                                check &= checkStructure.checkDataConsistency(autoRepair2);
                                if (!check) {
                                    datasourceFactory.setValid(false);
                                }
                            }
                        } catch (CMSUnavailableException cmsue) {
                            logger.error("Error while checking datasource for sanity", cmsue);
                            datasourceFactory.setValid(false);
                        }
                    }
                    // for valid datasource factories, schedule background jobs
                    if (datasourceFactory.isValid()) {
                        startScheduler();
                        datasourceFactory.scheduleJobs(scheduler);
                    }
                }
                // for invalid datasource factories, do not return datasources
                if (!datasourceFactory.isValid()) {
                    ds = null;
                }
            }
            return ds;
        }
    }

    /**
     * Returns a changeable content object with the given content id.
     * @param contentId The content id of the object which should be returned.
     *        ([objecttype].[object id] e.g. 10002.123)
     * @param datasource Writeable Datasource used to load the content object.
     * @return the content object corresponding to the given content id.
     * @throws DatasourceNotAvailableException
     */
    public static Changeable getChangeableContentObject(String contentId,
            WriteableDatasource datasource) throws DatasourceNotAvailableException {
        return (Changeable) getContentObject(contentId, datasource);
    }

    /**
     * Start the scheduler
     */
    protected static synchronized void startScheduler() {
        if (scheduler != null) {
            return;
        }
        SchedulerFactory factory = new StdSchedulerFactory();
        try {
            scheduler = factory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("Error while creating scheduler.", e);
        }
    }

    /**
     * Stop the scheduler. This method should be called before the factory is taken out of service
     */
    protected static synchronized void stopScheduler() {
        if (scheduler == null) {
            return;
        }

        try {
            scheduler.shutdown();
            scheduler = null;
        } catch (SchedulerException e) {
            logger.error("Error while stopping scheduler.", e);
        }
    }

    /**
     * Destroy all created datasource factories, close all handles (closing database connections)
     */
    protected static void destroyDatasourceFactories() {
        synchronized (datasourceFactories) {
            for (Iterator iterator = datasourceFactories.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                DatasourceFactoryImpl datasourceFactory = (DatasourceFactoryImpl) entry
                        .getValue();
                datasourceFactory.close();
            }

            datasourceFactories.clear();
        }

        synchronized(activeHandles) {
            for (Iterator iterator = activeHandles.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                SQLHandle handle = (SQLHandle)entry.getValue();
                handle.close();
            }

            activeHandles.clear();
        }
    }

    /**
     * Destroy the portal connector factory, close all database connections, remove the scheduler (created background threads).
     * This method must be called to take the PortalConnectorFactory out of service
     */
    public static void destroy() {
        // stop the scheduler
        stopScheduler();
        // destroy all factories
        destroyDatasourceFactories();
    }
}
