package mapping.entity;

import info.archinnov.achilles.annotations.WideRow;
import info.archinnov.achilles.type.WideMap;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * MultiKeyColumnFamilyBean
 * 
 * @author DuyHai DOAN
 * 
 */
@Entity
@WideRow
public class CompoundKeyWideRowBean
{
	

	@Id
	private Long id;

	@Column
	private WideMap<WideRowCompoundKey, String> map;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public WideMap<WideRowCompoundKey, String> getMap()
	{
		return map;
	}

}