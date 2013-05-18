package info.archinnov.achilles.dao;

import static info.archinnov.achilles.serializer.SerializerUtils.COMPOSITE_SRZ;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.entity.type.Pair;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GenericColumnFamilyDao
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftGenericWideRowDao extends ThriftAbstractDao
{

	private static final Logger log = LoggerFactory.getLogger(ThriftGenericWideRowDao.class);

	public <K, V> ThriftGenericWideRowDao(Cluster cluster, Keyspace keyspace, //
			String cf, //
			AchillesConsistencyLevelPolicy consistencyPolicy, Pair<K, V> rowkeyAndValueClasses)
	{

		super(cluster, keyspace, cf, consistencyPolicy, rowkeyAndValueClasses);
		columnNameSerializer = COMPOSITE_SRZ;
		log.debug(
				"Initializing GenericColumnFamilyDao for key serializer '{}', composite comparator and value serializer '{}'",
				keySerializer.getComparatorType().getTypeName(), valueSerializer
						.getComparatorType().getTypeName());
	}
}
