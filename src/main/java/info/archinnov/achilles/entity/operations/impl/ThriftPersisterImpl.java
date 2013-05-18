package info.archinnov.achilles.entity.operations.impl;

import static info.archinnov.achilles.helper.ThriftLoggerHelper.format;
import static info.archinnov.achilles.serializer.SerializerUtils.*;
import static me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL;
import info.archinnov.achilles.composite.factory.CompositeFactory;
import info.archinnov.achilles.dao.ThriftGenericWideRowDao;
import info.archinnov.achilles.entity.AchillesEntityIntrospector;
import info.archinnov.achilles.entity.context.ThriftPersistenceContext;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.JoinProperties;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.operations.AchillesEntityProxifier;
import info.archinnov.achilles.entity.operations.ThriftEntityPersister;
import info.archinnov.achilles.entity.operations.ThriftEntityProxifier;
import info.archinnov.achilles.entity.type.KeyValue;
import info.archinnov.achilles.exception.AchillesException;
import info.archinnov.achilles.validation.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.mutation.Mutator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThriftPersister
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftPersisterImpl
{
	private static final Logger log = LoggerFactory.getLogger(ThriftPersisterImpl.class);

	private AchillesEntityIntrospector introspector = new AchillesEntityIntrospector();
	private AchillesEntityProxifier proxifier = new ThriftEntityProxifier();

	private CompositeFactory compositeFactory = new CompositeFactory();

	public void batchPersistVersionSerialUID(ThriftPersistenceContext context)
	{
		Composite composite = new Composite();
		composite.setComponent(0, PropertyType.SERIAL_VERSION_UID.flag(), BYTE_SRZ, BYTE_SRZ
				.getComparatorType().getTypeName());
		composite.setComponent(1, PropertyType.SERIAL_VERSION_UID.name(), STRING_SRZ, STRING_SRZ
				.getComparatorType().getTypeName());
		composite.setComponent(2, 0, INT_SRZ, INT_SRZ.getComparatorType().getTypeName());
		Long serialVersionUID = introspector.findSerialVersionUID(context.getEntityClass());

		if (serialVersionUID != null)
		{
			if (log.isTraceEnabled())
			{
				log.trace(
						"Batch persisting serial version UID for entity of class {} and primary key {} with column name {}",
						context.getEntityClass().getCanonicalName(), context.getPrimaryKey(),
						format(composite));
			}
			context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), composite,
					serialVersionUID.toString(),
					context.getEntityMutator(context.getColumnFamilyName()));
		}
		else
		{
			throw new AchillesException("Cannot find 'serialVersionUID' field for entity class '"
					+ context.getEntityClass().getCanonicalName() + "'");
		}
	}

	public void batchPersistSimpleProperty(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		Composite name = compositeFactory.createForBatchInsertSingleValue(propertyMeta);
		String value = propertyMeta.writeValueToString(introspector.getValueFromField(
				context.getEntity(), propertyMeta.getGetter()));
		if (value != null)
		{
			if (log.isTraceEnabled())
			{
				log.trace(
						"Batch persisting simple property {} from entity of class {} and primary key {} with column name {}",
						propertyMeta.getPropertyName(),
						context.getEntityClass().getCanonicalName(), context.getPrimaryKey(),
						format(name));
			}
			context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name, value,
					context.getEntityMutator(context.getColumnFamilyName()));
		}
	}

	public <V> void batchPersistList(List<V> list, ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		int count = 0;
		for (V value : list)
		{
			Composite name = compositeFactory.createForBatchInsertMultiValue(propertyMeta, count);

			String stringValue = propertyMeta.writeValueToString(value);
			if (stringValue != null)
			{
				if (log.isTraceEnabled())
				{
					log.trace(
							"Batch persisting list property {} from entity of class {} and primary key {} with column name {}",
							propertyMeta.getPropertyName(), context.getEntityClass()
									.getCanonicalName(), context.getPrimaryKey(), format(name));
				}
				context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name,
						stringValue, context.getEntityMutator(context.getColumnFamilyName()));
			}
			count++;
		}
	}

	public <V> void batchPersistSet(Set<V> set, ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		for (V value : set)
		{
			Composite name = compositeFactory.createForBatchInsertMultiValue(propertyMeta,
					value.hashCode());

			String stringValue = propertyMeta.writeValueToString(value);
			if (stringValue != null)
			{
				if (log.isTraceEnabled())
				{
					log.trace(
							"Batch persisting set property {} from entity of class {} and primary key {} with column name {}",
							propertyMeta.getPropertyName(), context.getEntityClass()
									.getCanonicalName(), context.getPrimaryKey(), format(name));
				}
				context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name,
						stringValue, context.getEntityMutator(context.getColumnFamilyName()));
			}
		}
	}

	public <K, V> void batchPersistMap(Map<K, V> map, ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		for (Entry<K, V> entry : map.entrySet())
		{
			Composite name = compositeFactory.createForBatchInsertMultiValue(propertyMeta, entry
					.getKey().hashCode());

			String value = propertyMeta.writeValueToString(new KeyValue<K, V>(entry.getKey(), entry
					.getValue()));

			if (log.isTraceEnabled())
			{
				log.trace(
						"Batch persisting map property {} from entity of class {} and primary key {} with column name {}",
						propertyMeta.getPropertyName(),
						context.getEntityClass().getCanonicalName(), context.getPrimaryKey(),
						format(name));
			}
			context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name, value,
					context.getEntityMutator(context.getColumnFamilyName()));
		}
	}

	public <V> void batchPersistJoinEntity(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta, V joinEntity, ThriftEntityPersister persister)
	{
		JoinProperties joinProperties = propertyMeta.getJoinProperties();
		PropertyMeta<?, ?> idMeta = propertyMeta.joinIdMeta();

		Object joinId = introspector.getKey(joinEntity, idMeta);
		Validator.validateNotNull(joinId, "Primary key for join entity '" + joinEntity
				+ "' should not be null");
		String joinIdString = idMeta.writeValueToString(joinId);

		Composite joinComposite = compositeFactory.createForBatchInsertSingleValue(propertyMeta);
		context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), joinComposite,
				joinIdString, context.getEntityMutator(context.getColumnFamilyName()));

		ThriftPersistenceContext joinPersistenceContext = (ThriftPersistenceContext) context
				.newPersistenceContext(propertyMeta.joinMeta(), proxifier.unproxy(joinEntity));

		if (log.isTraceEnabled())
		{
			log.trace(
					"Batch persisting join primary key for property {} from entity of class {} and primary key {} with column name {}",
					propertyMeta.getPropertyName(), context.getEntityClass().getCanonicalName(),
					context.getPrimaryKey(), format(joinComposite));
		}
		persister.cascadePersistOrEnsureExists(joinPersistenceContext, joinEntity, joinProperties);
	}

	public <V> void batchPersistJoinCollection(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta, Collection<V> joinCollection,
			ThriftEntityPersister persister)
	{
		JoinProperties joinProperties = propertyMeta.getJoinProperties();
		EntityMeta joinEntityMeta = joinProperties.getEntityMeta();
		PropertyMeta<?, ?> joinIdMeta = joinEntityMeta.getIdMeta();
		int count = 0;
		for (V joinEntity : joinCollection)
		{
			Composite name = compositeFactory.createForBatchInsertMultiValue(propertyMeta, count);

			Object joinEntityId = introspector
					.getValueFromField(joinEntity, joinIdMeta.getGetter());

			String joinEntityIdStringValue = joinIdMeta.writeValueToString(joinEntityId);
			if (joinEntityIdStringValue != null)
			{
				if (log.isTraceEnabled())
				{
					log.trace(
							"Batch persisting join primary keys for property {} from entity of class {} and primary key {} with column name {}",
							propertyMeta.getPropertyName(), context.getEntityClass()
									.getCanonicalName(), context.getPrimaryKey(), format(name));
				}
				context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name,
						joinEntityIdStringValue,
						context.getEntityMutator(context.getColumnFamilyName()));

				ThriftPersistenceContext joinPersistenceContext = (ThriftPersistenceContext) context
						.newPersistenceContext(propertyMeta.joinMeta(),
								proxifier.unproxy(joinEntity));

				persister.cascadePersistOrEnsureExists(joinPersistenceContext, joinEntity,
						joinProperties);
			}
			count++;
		}
	}

	public <K, V> void batchPersistJoinMap(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta, Map<K, V> joinMap, ThriftEntityPersister persiter)
	{
		JoinProperties joinProperties = propertyMeta.getJoinProperties();
		EntityMeta joinEntityMeta = joinProperties.getEntityMeta();
		PropertyMeta<?, ?> idMeta = joinEntityMeta.getIdMeta();

		for (Entry<K, V> entry : joinMap.entrySet())
		{
			Composite name = compositeFactory.createForBatchInsertMultiValue(propertyMeta, entry
					.getKey().hashCode());

			V joinEntity = entry.getValue();
			Object joinEntityId = introspector.getValueFromField(joinEntity, idMeta.getGetter());
			String joinEntityIdStringValue = idMeta.writeValueToString(joinEntityId);

			String value = propertyMeta.writeValueToString(new KeyValue<K, String>(entry.getKey(),
					joinEntityIdStringValue));
			context.getEntityDao().insertColumnBatch(context.getPrimaryKey(), name, value,
					context.getEntityMutator(context.getColumnFamilyName()));

			ThriftPersistenceContext joinPersistenceContext = (ThriftPersistenceContext) context
					.newPersistenceContext(propertyMeta.joinMeta(), proxifier.unproxy(joinEntity));

			if (log.isTraceEnabled())
			{
				log.trace(
						"Batch persisting join primary keys for property {} from entity of class {} and primary key {} with column name {}",
						propertyMeta.getPropertyName(),
						context.getEntityClass().getCanonicalName(), context.getPrimaryKey(),
						format(name));
			}
			persiter.cascadePersistOrEnsureExists(joinPersistenceContext, joinEntity,
					joinProperties);
		}
	}

	public void remove(ThriftPersistenceContext context)
	{
		EntityMeta entityMeta = context.getEntityMeta();
		Object primaryKey = context.getPrimaryKey();

		if (context.isWideRow())
		{
			log.trace("Batch removing wide row of class {} and primary key {}", context
					.getEntityClass().getCanonicalName(), context.getPrimaryKey());
			Mutator<Object> wideRowMutator = context.getWideRowMutator(entityMeta
					.getColumnFamilyName());
			context.getColumnFamilyDao().removeRowBatch(primaryKey, wideRowMutator);
		}
		else
		{

			log.trace("Batch removing entity of class {} and primary key {}", context
					.getEntityClass().getCanonicalName(), context.getPrimaryKey());

			Mutator<Object> entityMutator = context.getEntityMutator(entityMeta
					.getColumnFamilyName());
			context.getEntityDao().removeRowBatch(primaryKey, entityMutator);
			for (Entry<String, PropertyMeta<?, ?>> entry : entityMeta.getPropertyMetas().entrySet())
			{
				PropertyMeta<?, ?> propertyMeta = entry.getValue();
				if (propertyMeta.isWideMap())
				{

					if (propertyMeta.isCounter())
					{
						removeCounterWideMap(context, propertyMeta);
					}
					else
					{
						removeWideMap(context, primaryKey, propertyMeta);
					}
				}
				if (propertyMeta.isCounter())
				{
					removeSimpleCounter(context, propertyMeta);
				}
			}
		}
	}

	private void removeWideMap(ThriftPersistenceContext context, Object primaryKey,
			PropertyMeta<?, ?> propertyMeta)
	{
		log.trace("Batch removing wideMap property {} of class {} and primary key {}",
				propertyMeta.getPropertyName(), context.getEntityClass().getCanonicalName(),
				context.getPrimaryKey());

		String externalColumnFamilyName = propertyMeta.getExternalCFName();
		ThriftGenericWideRowDao findColumnFamilyDao = context
				.findWideRowDao(externalColumnFamilyName);
		findColumnFamilyDao.removeRowBatch(primaryKey,
				context.getWideRowMutator(externalColumnFamilyName));
	}

	public void removePropertyBatch(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		Composite start = compositeFactory
				.createBaseForQuery(propertyMeta, ComponentEquality.EQUAL);
		Composite end = compositeFactory.createBaseForQuery(propertyMeta, GREATER_THAN_EQUAL);

		if (log.isTraceEnabled())
		{
			log.trace(
					"Batch removing property {} of class {} and primary key {} with column names {}  / {}",
					propertyMeta.getPropertyName(), context.getEntityClass().getCanonicalName(),
					context.getPrimaryKey(), format(start), format(end));
		}
		context.getEntityDao().removeColumnRangeBatch(context.getPrimaryKey(), start, end,
				context.getEntityMutator(context.getColumnFamilyName()));
	}

	private void removeSimpleCounter(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{
		Composite keyComp = compositeFactory.createKeyForCounter(propertyMeta.fqcn(),
				context.getPrimaryKey(), propertyMeta.counterIdMeta());
		Composite com = compositeFactory.createForBatchInsertSingleCounter(propertyMeta);

		log.trace("Batch removing counter property {} of class {} and primary key {}",
				propertyMeta.getPropertyName(), context.getEntityClass().getCanonicalName(),
				context.getPrimaryKey());

		context.getCounterDao().removeCounterBatch(keyComp, com, context.getCounterMutator());
	}

	private void removeCounterWideMap(ThriftPersistenceContext context,
			PropertyMeta<?, ?> propertyMeta)
	{

		log.trace("Batch removing counter wideMap property {} of class {} and primary key {}",
				propertyMeta.getPropertyName(), context.getEntityClass().getCanonicalName(),
				context.getPrimaryKey());

		Composite keyComp = compositeFactory.createKeyForCounter(propertyMeta.fqcn(),
				context.getPrimaryKey(), propertyMeta.counterIdMeta());
		context.getCounterDao().removeCounterRowBatch(keyComp, context.getCounterMutator());
	}
}
