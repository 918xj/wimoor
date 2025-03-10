package com.wimoor.erp.material.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wimoor.api.erp.assembly.pojo.vo.AssemblyVO;
import com.wimoor.common.GeneralUtil;
import com.wimoor.common.mvc.BizException;
import com.wimoor.common.mvc.FileUpload;
import com.wimoor.common.pojo.entity.Picture;
import com.wimoor.common.result.Result;
import com.wimoor.common.service.IPictureService;
import com.wimoor.common.service.impl.PictureServiceImpl;
import com.wimoor.common.user.UserInfo;
import com.wimoor.erp.api.AdminClientOneFeign;
import com.wimoor.erp.api.AmazonClientOneFeign;
import com.wimoor.erp.assembly.pojo.entity.Assembly;
import com.wimoor.erp.assembly.service.IAssemblyService;
import com.wimoor.erp.common.pojo.entity.ERPBizException;
import com.wimoor.erp.common.pojo.entity.EnumByInventory;
import com.wimoor.erp.customer.pojo.entity.Customer;
import com.wimoor.erp.customer.service.ICustomerService;
import com.wimoor.erp.inventory.mapper.InventoryMapper;
import com.wimoor.erp.inventory.pojo.entity.InventoryParameter;
import com.wimoor.erp.inventory.service.IInventoryService;
import com.wimoor.erp.inventory.service.IStockCycleService;
import com.wimoor.erp.material.mapper.ERPMaterialHistoryMapper;
import com.wimoor.erp.material.mapper.MaterialBrandMapper;
import com.wimoor.erp.material.mapper.MaterialCategoryMapper;
import com.wimoor.erp.material.mapper.MaterialConsumableMapper;
import com.wimoor.erp.material.mapper.MaterialCustomsFileMapper;
import com.wimoor.erp.material.mapper.MaterialCustomsItemMapper;
import com.wimoor.erp.material.mapper.MaterialCustomsMapper;
import com.wimoor.erp.material.mapper.MaterialMapper;
import com.wimoor.erp.material.mapper.MaterialSupplierMapper;
import com.wimoor.erp.material.mapper.MaterialSupplierStepwiseMapper;
import com.wimoor.erp.material.mapper.MaterialTagsMapper;
import com.wimoor.erp.material.pojo.dto.MaterialDTO;
import com.wimoor.erp.material.pojo.dto.PlanDTO;
import com.wimoor.erp.material.pojo.entity.DimensionsInfo;
import com.wimoor.erp.material.pojo.entity.ERPMaterialHistory;
import com.wimoor.erp.material.pojo.entity.Material;
import com.wimoor.erp.material.pojo.entity.MaterialBrand;
import com.wimoor.erp.material.pojo.entity.MaterialCategory;
import com.wimoor.erp.material.pojo.entity.MaterialConsumable;
import com.wimoor.erp.material.pojo.entity.MaterialCustoms;
import com.wimoor.erp.material.pojo.entity.MaterialCustomsItem;
import com.wimoor.erp.material.pojo.entity.MaterialMark;
import com.wimoor.erp.material.pojo.entity.MaterialSupplier;
import com.wimoor.erp.material.pojo.entity.MaterialSupplierStepwise;
import com.wimoor.erp.material.pojo.entity.MaterialTags;
import com.wimoor.erp.material.pojo.entity.StepWisePrice;
import com.wimoor.erp.material.pojo.vo.MaterialConsumableVO;
import com.wimoor.erp.material.pojo.vo.MaterialInfoVO;
import com.wimoor.erp.material.pojo.vo.MaterialSupplierVO;
import com.wimoor.erp.material.pojo.vo.MaterialVO;
import com.wimoor.erp.material.service.IDimensionsInfoService;
import com.wimoor.erp.material.service.IMaterialCategoryService;
import com.wimoor.erp.material.service.IMaterialMarkService;
import com.wimoor.erp.material.service.IMaterialService;
import com.wimoor.erp.material.service.IStepWisePriceService;
import com.wimoor.erp.material.service.IZipRarUploadService;
import com.wimoor.erp.purchase.pojo.entity.PurchasePlanItem;
import com.wimoor.erp.purchase.service.IPurchasePlanItemService;
import com.wimoor.erp.ship.pojo.dto.ConsumableOutFormDTO;
import com.wimoor.erp.stock.mapper.OutWarehouseFormEntryMapper;
import com.wimoor.erp.stock.mapper.OutWarehouseFormMapper;
import com.wimoor.erp.stock.pojo.entity.OutWarehouseForm;
import com.wimoor.erp.stock.pojo.entity.OutWarehouseFormEntry;
import com.wimoor.erp.warehouse.service.IWarehouseService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
 
 

@Service("materialService")
@RequiredArgsConstructor
public class MaterialServiceImpl extends  ServiceImpl<MaterialMapper,Material> implements IMaterialService {
	 
	final ERPMaterialHistoryMapper erpMaterialHistoryMapper;
	 
	final IMaterialMarkService materialMarkService;
	 
	final MaterialCategoryMapper materialCategoryMapper;
	 
	final IAssemblyService assemblyService;
	 
	final IStockCycleService stockCycleService;
	 
	final IStepWisePriceService stepWisePriceService;
	 
	final IPictureService pictureService;
	 
	final IDimensionsInfoService dimensionsInfoService;
	 
	final IMaterialCategoryService materialCategoryService;
	 
	final IPurchasePlanItemService purchasePlanItemService;
	 
	final IZipRarUploadService zipRarUploadService;
	 
	final ICustomerService customerService;
	 
	final MaterialSupplierStepwiseMapper materialSupplierStepwiseMapper;
	 
	final MaterialSupplierMapper materialSupplierMapper;
	 
	final MaterialConsumableMapper materialConsumableMapper;
	 
	final IWarehouseService warehouseService;
	 
	final OutWarehouseFormMapper outWarehouseFormMapper;
	 
	final OutWarehouseFormEntryMapper outWarehouseFormEntryMapper;
	 
	final InventoryMapper inventoryMapper;
	
	final FileUpload fileUpload;
	final MaterialTagsMapper materialTagsMapper;
	@Lazy
	@Autowired
	IInventoryService inventoryService;
	 
	final MaterialCustomsMapper materialCustomsMapper;
	 
	final MaterialCustomsFileMapper materialCustomsFileMapper;
	final AmazonClientOneFeign amazonClientOneFeign;
	final AdminClientOneFeign adminClientOneFeign;
	final MaterialCustomsItemMapper materialCustomsItemMapper;
	final MaterialBrandMapper materialBrandMapper;
	
	public MaterialVO findMaterialById(String id) {
		return this.baseMapper.findMaterialById(id);
	}

	public List<String> getmskuList(List<String> list) {
		return materialTagsMapper.getmskuList(list);
	}
	
	public IPage<Map<String, Object>> findByCondition(Page<?> page,Map<String, Object> params, MaterialDTO dto) {
		List<String> taglist = dto.getTaglist();//123,456,122,
		if(taglist!=null && taglist.size()>0) {
			List<String> midlist=	materialTagsMapper.getMidList(taglist);
			params.put("midlist", midlist);
		}
	    IPage<Map<String, Object>> list = this.baseMapper.findMaterial(page,params);
	    if(list!=null && list.getRecords().size()>0) {
	    	 for(int i=0;i<list.getRecords().size();i++) {
	    		 Map<String, Object> item = list.getRecords().get(i);
	    		 String mid=item.get("id").toString();
	    		 List<Map<String, Object>> historylist = this.selectProPriceHisById(mid);
	    		 String pricestr="";
	    		 if(historylist!=null && historylist.size()>0) {
	    			 for(int j=0;j<historylist.size();j++) {
	    				 Map<String,Object> history = historylist.get(j);
	    				 if(history!=null) {
	    					 if(history.get("price")!=null) {
	    						 String price=history.get("price").toString();
								 int len = price.indexOf(".");
								 price=price.substring(0, len+3);
								 pricestr+="历史价格("+
					    				   GeneralUtil.formatDate((Date)history.get("opttime"))+
					    				   "): "+price+"<br/>";
							 }
	    				 }
	    			 }
	    			 if(GeneralUtil.isEmpty(pricestr)) {
	    				 pricestr="暂无价格历史!";
	    			 }
	    		 }else {
	    			 pricestr="暂无价格历史!";
	    		 }
	    		 item.put("pricestr", pricestr);
	    		 item.put("itemshow", false);
				LambdaQueryWrapper<MaterialTags> queryTagsIdsList=new LambdaQueryWrapper<MaterialTags>();
				queryTagsIdsList.eq(MaterialTags::getMid,mid);
				Set<String> tagsIdsList=new HashSet<String>();
				List<MaterialTags>  materialTagsList= materialTagsMapper.selectList(queryTagsIdsList);
				for(MaterialTags tagitem:materialTagsList) {
					tagsIdsList.add(tagitem.getTagid());
				}
				try {
					Result<List<Map<String,Object>>> tagnamelistResult=adminClientOneFeign.findTagsNameByIds(tagsIdsList);
					List<Map<String,Object>> tagnamelist=tagnamelistResult.getData();
					item.put("TagNameList",tagnamelist);
				}catch(Exception e) {
					e.printStackTrace();
				}
	    	 }
	    }
		return list;
	}

	public List<Map<String, Object>> findAllByCondition(Map<String, Object> map) {
		return  this.baseMapper.findAllByCondition(map);
	}
	
	public boolean saveMark(String materialid, String type, String content,String userid) throws ERPBizException {
		MaterialMark materialmark = new MaterialMark();
		materialmark.setFtype(type);
		materialmark.setMaterialid(materialid);
		materialmark.setMark(content);
		materialmark.setOpttime(new Date());
		materialmark.setOperator(userid);
		QueryWrapper<MaterialMark> queryWrapper=new QueryWrapper<MaterialMark>();
		queryWrapper.eq("materialid",materialid);
		queryWrapper.eq("ftype",type);
		MaterialMark old = materialMarkService.getOne(queryWrapper);
		if (old != null) {
			return materialMarkService.updateById(materialmark);
		} else {
			return materialMarkService.save(materialmark);
		}
	}

	public String getNotice(String id) {
		QueryWrapper<MaterialMark> queryWrapper=new QueryWrapper<MaterialMark>();
		queryWrapper.eq("materialid",id);
		queryWrapper.eq("ftype","notice");
		MaterialMark old = materialMarkService.getOne(queryWrapper);
		return old.getMark();
	}

	public List<Material> selectAllSKUForSelect(String sku, String shopid) {
		return this.baseMapper.selectAllSKUForSelect(sku, shopid);
	}

	public List<Map<String, Object>> selectAllSKUForLabel(String sku, String shopid) {
		return this.baseMapper.selectAllSKUForLabel(sku, shopid);
	}

	public List<MaterialCategory> selectAllCateByShopid(String shopid) {
		QueryWrapper<MaterialCategory> queryWrapper=new QueryWrapper<MaterialCategory>();
		queryWrapper.eq("shopid", shopid);
		queryWrapper.orderByAsc("name");
		List<MaterialCategory> list = materialCategoryMapper.selectList(queryWrapper);
		return list;
	}

	@Override
	public boolean delete(String id) {
		Material material = this.getById(id);
		// 如果ID存在，删除图片对应位置
		Picture oldpicture = pictureService.getById(material.getImage());
		if (oldpicture != null && GeneralUtil.isNotEmpty(oldpicture.getLocation())) {
			pictureService.removeById(material.getImage());
		}
		dimensionsInfoService.removeById(material.getItemdimensions());
		dimensionsInfoService.removeById(material.getPkgdimensions());
		dimensionsInfoService.removeById(material.getBoxdimensions());
		stepWisePriceService.deleteByMaterial(id.toString());
		assemblyService.deleteByMainmid(id.toString());
		stockCycleService.deleteByMaterial(id.toString());
		return this.removeById(id);
	}
	
	MaterialCustoms saveMaterialCustoms(MaterialInfoVO vo,Material material){
    	//海关信息
		MaterialCustoms customsvo = vo.getCustoms();
		//String customscode=vo.getCustoms();//   海关编码
		String engname=customsvo.getNameEn();//   产品英文名
		String chnname=customsvo.getNameCn();//   产品中文名
		//String stuff=request.getParameter("stuff");//   产品材质
		String materialmodel=customsvo.getModel();//   产品型号
		String materialuse=customsvo.getMaterialUse();//   产品用途
		//String materialbrand=request.getParameter("materialbrand");//   产品品牌
		boolean isele=customsvo.getIselectricity();//   是否带电/磁
		boolean isdanger=customsvo.getIsdanger();//   是否危险品
		BigDecimal unitprice = customsvo.getUnitprice();
		BigDecimal addfee = customsvo.getAddfee();//附加费用
	 
		MaterialCustoms customs=new MaterialCustoms();
		customs.setMaterialid(material.getId());
		customs.setIselectricity(isele);
		customs.setModel(materialmodel);
		customs.setNameCn(chnname);
		customs.setBrand(customsvo.getBrand());
		customs.setMaterial(customsvo.getMaterial());
		customs.setMaterialUse(materialuse);
		customs.setNameEn(engname);
		customs.setIsdanger(isdanger);
		customs.setUnitprice(unitprice);
		customs.setAddfee(addfee);
		customs.setCurrency(customsvo.getCurrency());
		//先删除再加
		materialCustomsMapper.deleteById(material.getId());
		materialCustomsMapper.insert(customs);
		List<MaterialCustomsItem> customsItemList = vo.getCustomsItemList();
		if(customsItemList!=null && customsItemList.size()>0) {
			//先删除后加
			QueryWrapper<MaterialCustomsItem> queryWrapper=new QueryWrapper<MaterialCustomsItem>();
			queryWrapper.eq("materialid", material.getId());
			materialCustomsItemMapper.delete(queryWrapper);
			for (int i = 0; i < customsItemList.size(); i++) {
				MaterialCustomsItem item = customsItemList.get(i);
				if(StrUtil.isNotEmpty(item.getCode()) && item.getFee()!=null && item.getTaxrate()!=null) {
					MaterialCustomsItem entity=new MaterialCustomsItem();
					entity.setMaterialid(material.getId());
					entity.setCode(item.getCode());
					entity.setCountry(item.getCountry());
					entity.setFee(item.getFee());
					entity.setTaxrate(item.getTaxrate());
					entity.setCurrency(item.getCurrency());
					materialCustomsItemMapper.insert(entity);
				}
				
			}
		}
		return customs;
    }
	
	public void saveAllInfo(MaterialInfoVO vo,MultipartFile file, UserInfo user) throws ERPBizException {
		if(vo==null) {
			throw new ERPBizException("填入数据参数异常！");
		}
		Material material=saveBaseInfo(vo,file,user);
		saveMaterialCustoms(vo,material);
		saveMaterialAssembly(vo,material);
		// 获取物料基本信息
		List<MaterialSupplierVO> supplierlist = vo.getSupplierList();
		saveOrUpdateSupplier(supplierlist,user,material.getId(),material);
		List<MaterialConsumableVO> consumableList = vo.getConsumableList();
		saveMaterialConsumable(consumableList,user,material.getId());
		saveTags(vo,material,user);
		return ;
	}
	
	private void saveTags(MaterialInfoVO vo, Material material, UserInfo user) {
		// TODO Auto-generated method stub
		//标签添加对应关系 先删除后加
		QueryWrapper<MaterialTags> delqueryWrapper=new QueryWrapper<MaterialTags>();
		delqueryWrapper.eq("mid", material.getId());
		materialTagsMapper.delete(delqueryWrapper);
		if(StrUtil.isNotEmpty(vo.getTaglist())) {
			String taglist = vo.getTaglist();
			String[] tagLists = taglist.split(",");
			if(tagLists!=null && tagLists.length>0) {
				for (int i = 0; i < tagLists.length; i++) {
					String tagid = tagLists[i];
					if(StrUtil.isNotEmpty(tagid)) {
						MaterialTags tag=new MaterialTags();
						tag.setMid(material.getId());
						tag.setOperator(user.getId());
						tag.setOpttime(new Date());
						tag.setTagid(tagid);
						materialTagsMapper.insert(tag);
					}
				}
			}
		}
	}

	private void saveMaterialAssembly(MaterialInfoVO vo, Material material) {
		// TODO Auto-generated method stub
		List<AssemblyVO> asslist = vo.getAssemblyList();
		List<AssemblyVO> volist = assemblyService.selectByMainmid(material.getId());
		Set<String> subset=new HashSet<String>();
		for(AssemblyVO item:volist) {
			subset.add(item.getSubmid());
		}
		assemblyService.deleteByMainmid(material.getId());
		if ("1".equals(material.getIssfg()) && asslist!=null && asslist.size()>0) {
			for (int i = 0; i < asslist.size(); i++) {
				AssemblyVO assitem = asslist.get(i);
				Assembly assembly = new Assembly();
				assembly.setMainmid(material.getId());
				assembly.setSubmid(assitem.getSubmid());
				assembly.setSubnumber(assitem.getSubnumber());
				assembly.setRemark(assitem.getRemark());
				assembly.setOperator(material.getOperator());
				assemblyService.save(assembly);
				Material assub = this.getById(assitem.getSubmid());
				assub.setIssfg("2");
				this.updateById(assub);
				subset.remove(assub.getId());
			}
		}else {
			if(material.getIssfg()==null||material.getIssfg().equals("1")) {
				Material main = this.getById(material.getId());
				main.setIssfg("0");
				this.updateById(main);	
			}
		}
		for(String id:subset) {
			LambdaQueryWrapper<Assembly> asquery=new LambdaQueryWrapper<Assembly>();
			asquery.eq(Assembly::getSubmid,id);
			long count = assemblyService.count(asquery);
			if(count==0) {
				Material assub = this.getById(id);
				assub.setIssfg("0");
				this.updateById(assub);	
			}
		}
		
	}

	//供应商列表
	public void saveOrUpdateSupplier(List<MaterialSupplierVO> supplierList,UserInfo user,String id,Material material) {
		//一进来先删除当前的列表 以最新的supplierlist为准
 
		QueryWrapper<MaterialSupplier> queryWrapper=new QueryWrapper<MaterialSupplier>();
		queryWrapper.eq("materialid",id);
		materialSupplierMapper.delete(queryWrapper);
	 
		QueryWrapper<MaterialSupplierStepwise> queryWrapper2=new QueryWrapper<MaterialSupplierStepwise>();
		queryWrapper2.eq("materialid",id);
		materialSupplierStepwiseMapper.delete(queryWrapper2);
		stepWisePriceService.deleteByMaterial(id);
		//supplierListpara="["+supplierListpara+"]";
		//JSONArray jsonArray = GeneralUtil.getJsonArray(supplierListpara);
		//List<Map<String, Object>> supplierList = GeneralUtil.jsonStringToMapList(supplierListpara);
		if(supplierList!=null && supplierList.size()>0) {
			for(int i=0;i<supplierList.size();i++) {
				MaterialSupplierVO item = supplierList.get(i);
				MaterialSupplier materialSupplier=new MaterialSupplier();
				String supid = item.getSupplierid();
				boolean isdefault=item.getIsdefault();
				String procode = item.getProductCode();
				BigDecimal costother= item.getOtherCost();
				String purchaseurl = item.getPurchaseUrl();
				BigDecimal badRate= item.getBadrate();
				Integer moq=item.getMOQ();
				List<MaterialSupplierStepwise> pricelist = item.getStepList();
				BigDecimal maxprice = new BigDecimal("0");
				if(pricelist!=null && pricelist.size()>0) {
					for(int j=0;j<pricelist.size();j++) {
						MaterialSupplierStepwise step = pricelist.get(j);
						int amount= step.getAmount();
						if(j==0 && (moq==null || moq<=0)) {
							moq=amount;
						}
						BigDecimal price=step.getPrice();
						if (price.toString().split("\\.").length > 1 && price.toString().split("\\.")[1].length() > 2) {
							throw new ERPBizException("阶梯采购采购价不能超过两位小数！");
						}
						if (maxprice.compareTo(price) < 0) {
							maxprice = price;
						}
						MaterialSupplierStepwise stepwise=new MaterialSupplierStepwise();
						stepwise.setAmount(amount);
						stepwise.setMaterialid(id);
						stepwise.setOperator(user.getId());
						stepwise.setOpttime(new Date());
						stepwise.setPrice(price);
						stepwise.setSupplierid(supid);
						materialSupplierStepwiseMapper.insert(stepwise);
					}
				}
				if(isdefault==true) {
					if(badRate!=null && badRate.compareTo(BigDecimal.ZERO)>0) {
						material.setBadrate(badRate.floatValue());
					}
					material.setSupplier(supid);
					material.setOtherCost(costother);
					material.setMOQ(moq);
					if(material.getPrice()==null) {
						material.setPrice(maxprice);
					}
					material.setProductCode(procode);
					material.setPurchaseUrl(purchaseurl);
					if(material.getId()!=null) {
						this.updateById(material);
					}
					if(pricelist!=null && pricelist.size()>0) {
						for(int j=0;j<pricelist.size();j++) {
							MaterialSupplierStepwise step = pricelist.get(j);
							int amount= step.getAmount();
							BigDecimal price=step.getPrice();
							if (price.toString().split("\\.").length > 1 && price.toString().split("\\.")[1].length() > 2) {
								throw new ERPBizException("阶梯采购采购价不能超过两位小数！");
							}
							StepWisePrice stepWisePrice = new StepWisePrice();
							stepWisePrice.setAmount(amount);
							stepWisePrice.setPrice(price);
							stepWisePrice.setMaterial(id);
							stepWisePrice.setOperator(user.getId());
							stepWisePriceService.save(stepWisePrice);
						}
					}
					
				}
				materialSupplier.setCreatedate(new Date());
				materialSupplier.setCreater(user.getId());
				materialSupplier.setIsdefault(isdefault);
				materialSupplier.setMaterialid(id);
				materialSupplier.setSupplierid(supid);
				materialSupplier.setOperator(user.getId());
				materialSupplier.setOpttime(new Date());
				materialSupplier.setOthercost(costother);
				materialSupplier.setProductcode(procode);
				materialSupplier.setPurchaseurl(purchaseurl);
				materialSupplier.setDeliverycycle(item.getDeliverycycle());
				if(badRate!=null && badRate.compareTo(BigDecimal.ZERO)>0) {
					materialSupplier.setBadrate(badRate.floatValue());
				}
				materialSupplier.setMOQ(moq);
				materialSupplierMapper.insert(materialSupplier);
			}
			
		}
	}
	
	//耗材列表
	public void saveMaterialConsumable(List<MaterialConsumableVO> list,UserInfo user,String id) {
		//一进来先删除当前的列表 以最新的ConsumableList为准
		QueryWrapper<MaterialConsumable> queryWrapper=new QueryWrapper<MaterialConsumable>();
		queryWrapper.eq("materialid",id);
		materialConsumableMapper.delete(queryWrapper);
		//ConsumableListpara="["+ConsumableListpara+"]";
		//JSONArray jsonArray = GeneralUtil.getJsonArray(ConsumableListpara);
		if(list!=null && list.size()>0) {
			for(int i=0;i<list.size();i++) {
				MaterialConsumableVO item = list.get(i);
				MaterialConsumable cons=new MaterialConsumable();
				cons.setAmount(new BigDecimal(item.getAmount()));
				cons.setMaterialid(id);
				cons.setOperator(user.getId());
				cons.setOpttime(new Date());
				cons.setSubmaterialid(item.getId());
				materialConsumableMapper.insert(cons);
			}
		}
	}

	public Material saveMaterial(DimensionsInfo itemdim,DimensionsInfo pkgdim,DimensionsInfo boxdim,MaterialInfoVO vo,UserInfo user) {
		MaterialVO materialvo=vo.getMaterial();
		Material material = new Material();
		material.setShopid(user.getCompanyid());
		material.setOperator(user.getId());
		material.setOpttime(new Date());
		material.setCreator(material.getOperator());
		material.setCreatedate(new Date());
		material.setPrice(materialvo.getPrice());
		material.setBoxnum(materialvo.getBoxnum());
		material.setVatrate(materialvo.getVatrate());
		if(GeneralUtil.isNotEmpty(materialvo.getCategoryid())) {
			material.setCategoryid(materialvo.getCategoryid());
		}
		material.setOwner(materialvo.getOwner());
		material.setSku(materialvo.getSku());
		material.setUpc(materialvo.getUpc());
		material.setName(materialvo.getName());
		material.setSmlAndLight(false);
		material.setBrand(materialvo.getBrandid());
		material.setSpecification(null);
		material.setIssfg(materialvo.getIssfg());
		material.setRemark(materialvo.getRemark());
		material.setColor(null);
		material.setDeliveryCycle(materialvo.getDeliveryCycle());
		material.setEffectivedate(materialvo.getEffectivedate());
		if(itemdim!=null) {
			material.setItemdimensions(itemdim.getId());
		}
		if(boxdim!=null) {
			material.setBoxdimensions(boxdim.getId());
		}
		if(pkgdim!=null) {
			material.setPkgdimensions(pkgdim.getId());
		}
		//组装周期
		if (materialvo.getAssemblyTime() != null) {
			int assemblyTime = materialvo.getAssemblyTime();
			material.setAssemblyTime(assemblyTime);
		} else {
			int assemblyTime = 0;
			material.setAssemblyTime(assemblyTime);
		}
		Material old=null;
		if(StrUtil.isNotBlank(materialvo.getId())) {
			old=this.baseMapper.selectById(materialvo.getId());
		}
		if(old!=null) {
			material.setId(old.getId());
			if(!old.getIssfg().equals(material.getIssfg())) {
				QueryWrapper<PurchasePlanItem> queryWrapper=new QueryWrapper<PurchasePlanItem>();
				queryWrapper.eq("materialid", materialvo.getId());
				queryWrapper.eq("status", 1);
				queryWrapper.eq("shopid", user.getCompanyid());
				List<PurchasePlanItem> purchaseList = purchasePlanItemService.list(queryWrapper);
				if (purchaseList != null && purchaseList.size() > 0) {
					throw new ERPBizException("已加入补货计划，请移除后再修改产品组装类别！");
				}
			}
			QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
			queryWrapper.eq("shopid", material.getShopid());
			queryWrapper.eq("sku", material.getSku());
			Material oldsku = this.baseMapper.selectOne(queryWrapper);
			if(!oldsku.getId().equals(material.getId())) {
				throw new ERPBizException("该SKU已经存在！");
			}else {
				  this.updateById(material);
				  ERPMaterialHistory his=new ERPMaterialHistory();
				  BeanUtil.copyProperties(material, his);
				  erpMaterialHistoryMapper.insert(his);
				  return material;
			}
		}else {
			QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
			queryWrapper.eq("shopid", material.getShopid());
			queryWrapper.eq("sku", material.getSku());
			List<Material> list = this.baseMapper.selectList(queryWrapper);
			if (list.size() > 0) {
				Material dbMaterial = list.get(0);
				if(dbMaterial.isDelete()) {
					throw new ERPBizException("产品SKU："+ dbMaterial.getSku() +"已经归档，请从归档状态还原后使用!");
				}else {
					throw new ERPBizException("该SKU已经存在！");
				}
			} else {
				  this.save(material);
				  ERPMaterialHistory his=new ERPMaterialHistory();
				  BeanUtil.copyProperties(material, his);
				  erpMaterialHistoryMapper.insert(his);
				  return material;
			}
		}
		
		
		
		
	}
	public Material saveBaseInfo(MaterialInfoVO vo,MultipartFile file, UserInfo user) throws  ERPBizException {
		
		DimensionsInfo itemdim=saveItemDim(vo);
		DimensionsInfo pkgdim=savePkgDim(vo);
		DimensionsInfo boxdim=saveBoxDim(vo);
		Material material =saveMaterial(itemdim,pkgdim,boxdim,vo,user);
		if("ok".equals(vo.getIscopy()) && file==null) {
			if(vo.getMaterial().getImageid()!=null) {
				Picture oldpic = pictureService.getById(vo.getMaterial().getImageid());
				if(oldpic!=null) {
					Picture pic=new Picture();
					pic.setHeight(oldpic.getHeight());
					pic.setHeightUnits(oldpic.getHeightUnits());
					pic.setLocation(oldpic.getLocation());
					pic.setUrl(oldpic.getUrl());
					pic.setWidth(oldpic.getWidth());
					pic.setWidthUnits(oldpic.getWidthUnits());
					boolean isok = pictureService.save(pic);
					if(isok==true) {
						material.setImage(pic.getId());
						this.updateById(material);
					}
				}
			}
		}
		//最后做图片处理
		if(file!=null) {
			//改变了图片
			try {
				this.uploadMaterialImg(user, material.getId(), file.getInputStream(), file.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return material;
	}

 

	DimensionsInfo saveDim(DimensionsInfo dimvo){
		BigDecimal length = dimvo.getLength();
		BigDecimal width =  dimvo.getWidth();
		BigDecimal height = dimvo.getHeight();
		BigDecimal weight = dimvo.getWeight();
		DimensionsInfo dim = new DimensionsInfo();
		dim.setLength(length);
		dim.setWidth(width);
		dim.setHeight(height);
		dim.setWeight(weight);
		DimensionsInfo old=dimensionsInfoService.getById(dimvo.getId());
		if(old!=null) {
			dim.setId(dimvo.getId());
			dimensionsInfoService.updateById(dim);
		}else {
			dimensionsInfoService.save(dim);
		}
		return dim;
	}
	
	
	private DimensionsInfo saveItemDim(MaterialInfoVO vo) {
        if(vo==null||vo.getItemDim()==null)return null;
		DimensionsInfo dimvo=vo.getItemDim();
		if(dimvo.getLength()==null &&  dimvo.getWidth()==null && dimvo.getHeight()==null && dimvo.getWeight()==null) {
			return null;
        }
		return saveDim(dimvo);
	}
	private DimensionsInfo savePkgDim(MaterialInfoVO vo) {
        if(vo==null||vo.getPkgDim()==null)return null;
		DimensionsInfo dimvo=vo.getPkgDim();
		if(dimvo.getLength()==null &&  dimvo.getWidth()==null && dimvo.getHeight()==null && dimvo.getWeight()==null) {
			return null;
        }
		return saveDim(dimvo);
	}
	private DimensionsInfo saveBoxDim(MaterialInfoVO vo) {
        if(vo==null||vo.getBoxDim()==null)return null;
		DimensionsInfo dimvo=vo.getBoxDim();
		if(dimvo.getLength()==null &&  dimvo.getWidth()==null && dimvo.getHeight()==null && dimvo.getWeight()==null) {
			return null;
        }
		return saveDim(dimvo);
	}


	public Map<String, Object> findDimAndAsinBymid(String sku, String shopid, String marketplaceid, String groupid) {
		return this.baseMapper.findDimAndAsinBymid(sku, shopid, marketplaceid, groupid);
	}

	public List<Map<String, Object>> getForSum(String shopid,String groupid) {
		return this.baseMapper.getForSum(shopid,groupid);
	}

	public Material findBySKU(String sku, String shopid) {
		QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
		queryWrapper.eq("sku", sku);
		queryWrapper.eq("shopid", shopid);
		queryWrapper.eq("isDelete", false);
		List<Material> materiallist = this.list(queryWrapper);
		if (materiallist.size() > 0) {
			return materiallist.get(0);
		}
		return null;
	}

	public String getImage(Material material) {
		Picture picture;
		if (material.getImage() != null) {
			picture = pictureService.getById(material.getImage());
			if (picture != null) {
				return picture.getLocation();
			}
		}
		return "images/systempicture/noimage40.png";
	}

	public String getImageByMaterialid(String id) {
		Material material = this.getById(id);
		if (material.getImage() != null) {
			Picture picture = pictureService.getById(material.getImage());
			if (picture != null) {
				return picture.getLocation();
			}
		}
		return "images/systempicture/noimage40.png";
	}

	public Map<String, BigDecimal> findDimensionsInfoBySKU(String sku, String shopid) {
		return this.baseMapper.findDimensionsInfoBySKU(sku, shopid);
	}

	public List<Map<String, Object>> getOwnerList(String shopid) {
		return this.baseMapper.getOwnerList(shopid);
	}

	@Cacheable(value = "materialListCache")
	public Map<String, Object> findMaterialMapBySku(String sku, String shopid) {
		return this.baseMapper.findMaterialMapBySku(sku, shopid);
	}

	@Cacheable(value = "materialListCache",key="#key")
	public List<String> findMarterialForColorOwner(String key,Map<String, Object> param) {
		return this.baseMapper.findMarterialForColorOwner(param);
	}

	@CacheEvict(value = "materialListCache", allEntries = true)
	public void logicalDeleteMaterial(UserInfo user, Material material) {
		if (material.isDelete()) {
			throw new ERPBizException(material.getSku() + "已经归档！请勿重复操作！");
		}
		material.setDelete(true);
		material.setOpttime(new Date());
		material.setOperator(user.getId());
		this.updateById(material);
	}

	@CacheEvict(value = "materialListCache", allEntries = true)
	public boolean updateReductionSKUMaterial(UserInfo user, String id, String sku) {
		QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
		queryWrapper.eq("sku", sku);
		queryWrapper.eq("shopid", user.getCompanyid());
		queryWrapper.eq("isDelete", false);
		List<Material> materiallist = this.list(queryWrapper);
		if (materiallist != null && materiallist.size() > 0) {
			throw new ERPBizException("该SKU:"+sku+"已经存在，不能还原回去！");
		} else {
			Material material = new Material();
			material.setSku(sku);
			material.setShopid(user.getCompanyid());
			material.setId(id);
			material.setOpttime(new Date());
			material.setOperator(user.getId());
			material.setDelete(false);
			return this.updateById(material);
		}
	}

	public boolean updateCycle(UserInfo user, String id, int amount) {
		Material materal = this.getById(id);
		if (materal != null) {
			materal.setDeliveryCycle(amount);
			return this.updateById(materal);
		} else {
			throw new ERPBizException("该产品不存在！");
		}
	}

	@SuppressWarnings("unchecked")
	public int updateItemMaterialByPrice(String[] ids, String priceMapList, UserInfo user) {
		int result = 0;
		for (int k = 0; k < ids.length; k++) {
			String materialid = ids[k];
			Material material = this.baseMapper.selectById(materialid);
			BigDecimal maxprice = new BigDecimal("0");
			if (GeneralUtil.isNotEmpty(priceMapList) && material != null) {
				String[] priceIdArr = priceMapList.split("},");
				List<StepWisePrice> oldPriceList = stepWisePriceService.selectByMaterial(material.getId());
				if (oldPriceList != null && oldPriceList.size() > 0) {
					stepWisePriceService.deleteByMaterial(material.getId());
				}
				for (int i = 0; i < priceIdArr.length; i++) {
					StepWisePrice stepWisePrice = new StepWisePrice();
					Map<String, Object> map = new HashMap<String, Object>();
					if (i == priceIdArr.length - 1) {
						map = (Map<String, Object>) JSON.parse(priceIdArr[i]);
					} else {
						map = (Map<String, Object>) JSON.parse(priceIdArr[i] + "}");
					}
					BigDecimal price = new BigDecimal(map.get("price").toString());
					if (maxprice.compareTo(price) < 0) {
						maxprice = price;
					}
					stepWisePrice.setAmount(Integer.parseInt(map.get("amount").toString()));
					stepWisePrice.setPrice(price);
					if (price.toString().split("\\.").length > 1 && price.toString().split("\\.")[1].length() > 2) {
						throw new ERPBizException("阶梯采购采购价不能超过两位小数！");
					}
					stepWisePrice.setMaterial(material.getId());
					stepWisePrice.setOperator(user.getId());
					stepWisePriceService.save(stepWisePrice);
				}
			}
			if (material != null) {
				material.setPrice(maxprice);
				material.setOpttime(new Date());
				material.setOperator(user.getId());
				if(this.updateById(material)) {
					result++ ;
				}
			}
		}
		return result;
	}

	public int updateItemMaterialByType(String[] ids, String ftype, String value, UserInfo user) {
		int result = 0;
		for (int k = 0; k < ids.length; k++) {
			String materialid = ids[k];
			Material material = this.baseMapper.selectById(materialid);
			if (material != null && GeneralUtil.isNotEmpty(value)) {
				if (ftype.equals("date")) {
					material.setEffectivedate(GeneralUtil.getDatez(value));
				} else if (ftype.equals("cycle")) {
					material.setDeliveryCycle(Integer.parseInt(value));
				} else if (ftype.equals("owner")) {
					material.setOwner(value);
				} else if (ftype.equals("supplier")) {
					material.setSupplier(value);
				} else if (ftype.equals("remark")) {
					material.setRemark(value);
				}
				material.setOpttime(new Date());
				material.setOperator(user.getId());
				if(this.updateById(material)) {
					result++ ;
				}
			} else {
				result += 0;
			}
		}
		return result;
	}

	public List<Map<String, Object>> findSKUImageForProduct(Map<String, Object> param) {
		return this.baseMapper.findSKUImageForProduct(param);
	}

	public List<Map<String, Object>> copyImageForProduct(List<Map<String, Object>> list, UserInfo user) {
		List<Map<String, Object>> errorList = new ArrayList<Map<String,Object>>();
	 	for(Map<String, Object> map : list) {
			//https://photo.wimoor.com/productImg/26138972975530085/A1F83G8C2ARO7P/51PglvullIL._SL75_.jpg
			String image = map.get("image").toString();
			String materialid = map.get("materialid").toString();
			Material material = this.baseMapper.selectById(materialid);
			String newPath =PictureServiceImpl.materialImgPath + user.getCompanyid() + "/";
				Picture picture=null;
				try {
					picture = pictureService.downloadPicture(image, newPath, material.getImage());
				} catch (Exception e) {
					e.printStackTrace();
				}
				material.setImage(picture.getId());
				this.baseMapper.updateById(material);
		 
		} 
		return errorList;
	}
	
	public List<Map<String, Object>> copyDimsForProduct(List<Map<String, Object>> list, UserInfo user) {
		List<Map<String, Object>> errorList = new ArrayList<Map<String,Object>>();
	 	for(Map<String, Object> map : list) {
			String dimsid = map.get("dims").toString();
			String materialid = map.get("materialid").toString();
			Material material = this.getById(materialid);
			DimensionsInfo dimension = dimensionsInfoService.getById(dimsid);
			if(material != null && dimension!=null) {
				String odldimid = material.getPkgdimensions();
				DimensionsInfo olddim = dimensionsInfoService.getById(odldimid);
				String lenunits=dimension.getLengthUnits();
				BigDecimal length = dimension.getLength();
				String widthunits=dimension.getWidthUnits();
				BigDecimal width = dimension.getWidth();
				String heightunits=dimension.getHeightUnits();
				BigDecimal height = dimension.getHeight();
				String weightunits=dimension.getWeightUnits();
				BigDecimal weight = dimension.getWeight();
				if(lenunits!=null) {
					if("inches".equals(lenunits)) {
						length=length.multiply(new BigDecimal("2.54")).setScale(2, BigDecimal.ROUND_DOWN);
						lenunits=null;
					}
				}
				if(widthunits!=null) {
					if("inches".equals(widthunits)) {
						width=width.multiply(new BigDecimal("2.54")).setScale(2, BigDecimal.ROUND_DOWN);
						widthunits=null;
					}
				}
				if(heightunits!=null) {
					if("inches".equals(heightunits)) {
						height=height.multiply(new BigDecimal("2.54")).setScale(2, BigDecimal.ROUND_DOWN);
						heightunits=null;
					}
				}
				if(weightunits!=null) {
					if("pounds".equals(weightunits)) {
						weight=weight.multiply(new BigDecimal("0.453")).setScale(2, BigDecimal.ROUND_DOWN);
						weightunits=null;
					}
				}
				if(olddim!=null) {
					//修改
					olddim.setLength(length);
					olddim.setLengthUnits(lenunits);
					olddim.setWidth(width);
					olddim.setWidthUnits(widthunits);
					olddim.setHeight(height);
					olddim.setHeightUnits(heightunits);
					olddim.setWeight(weight);
					olddim.setWeightUnits(weightunits);
					dimensionsInfoService.updateById(olddim);
				}else {
					//新增
					DimensionsInfo entity=new DimensionsInfo();
					entity.setLength(length);
					entity.setLengthUnits(lenunits);
					entity.setWidth(width);
					entity.setWidthUnits(widthunits);
					entity.setHeight(height);
					entity.setHeightUnits(heightunits);
					entity.setWeight(weight);
					entity.setWeightUnits(weightunits);
					dimensionsInfoService.save(entity);
				}
			}else {
				errorList.add(map);
			}
	 	}
		return errorList;
	}
	
	public List<Map<String, Object>> selectAllMaterialByShop(Map<String, Object> parammap) {
		List<Map<String, Object>> list = this.baseMapper.selectAllMaterialByShop(parammap);
		if(list!=null && list.size()>0) {
			for(int i=0;i<list.size();i++) {
				Map<String, Object> map = list.get(i);
				String materialid = map.get("id").toString();
				List<StepWisePrice> steplist = stepWisePriceService.selectByMaterial(materialid);
				if(steplist!=null && steplist.size()>0) {
					String stepPrice="";
					if(steplist.size()==1) {
						StepWisePrice step = steplist.get(0);
						map.put("stepPrice", step.getPrice());
						continue;
					}
					Map<Integer,Boolean> stepAmount=new HashMap<Integer, Boolean>();
					for(int j=0;j<steplist.size();j++) {
						StepWisePrice step = steplist.get(j);
						if(j==0) {
							stepAmount.put(step.getAmount(),true);
						}else {
							if(stepAmount.containsKey(step.getAmount())) {
								continue;
							}
						}
						stepPrice+=("{amount:"+step.getAmount()+",price:"+step.getPrice()+"},");
					}
					map.put("stepPrice", stepPrice.substring(0,stepPrice.length()-1));
				}else {
					map.put("stepPrice", map.get("price"));
				}
			}
		}
		return list;
	}

	public void getExcelMaterialAllInfoReport(SXSSFWorkbook workbook, List<Map<String, Object>> list) {
		Sheet sheet = workbook.createSheet("sheet1");
		List<String> titlelist = new ArrayList<String>();
		Map<String, String> titlechange = new HashMap<String, String>();
		titlelist.add("SKU");
		titlelist.add("产品名称");
		titlelist.add("带包装长-宽-高(cm)-重量(kg)");
		titlelist.add("采购单价(含阶梯价)");
		titlelist.add("类别");
		titlelist.add("组装清单");
		titlelist.add("耗材清单");
		titlelist.add("箱规长-宽-高(cm)-重量(kg)");
		titlelist.add("单箱数量");
		titlelist.add("品牌");
		titlelist.add("规格");
		titlelist.add("分类");
		titlelist.add("退税率");
		titlelist.add("产品负责人");
		titlelist.add("生效日期");
		titlelist.add("净产品长-宽-高(cm)-重量(kg)");
		titlelist.add("供应商");
		titlelist.add("供货周期");
		titlelist.add("不良率");
		titlelist.add("其他成本");
		titlelist.add("供应商代码");
		titlelist.add("采购链接");
		titlelist.add("颜色标签");
		titlelist.add("备注");
		titlelist.add("在途库存");
		titlelist.add("库存");
		titlelist.add("是否归档");

		titlechange.put("SKU", "sku");
		titlechange.put("产品名称", "name");
		titlechange.put("带包装长-宽-高(cm)-重量(kg)", "pageDimensions");
		titlechange.put("采购单价(含阶梯价)", "stepPrice");
		titlechange.put("类别", "issfg");
		titlechange.put("组装清单", "asslist");
		titlechange.put("耗材清单", "consumablelist");
		titlechange.put("品牌", "brand");
		titlechange.put("规格", "specification");
		titlechange.put("分类", "category");
		titlechange.put("退税率", "vatrate");
		titlechange.put("产品负责人", "owner");
		titlechange.put("生效日期", "effectivedate");
		titlechange.put("净产品长-宽-高(cm)-重量(kg)", "itemDimensions");
		titlechange.put("箱规长-宽-高(cm)-重量(kg)", "boxDimensions");
		titlechange.put("单箱数量", "boxnum");
		titlechange.put("供应商", "supplier");
		titlechange.put("供货周期", "deliverycycle");
		titlechange.put("不良率", "badrate");
		titlechange.put("其他成本", "othercost");
		titlechange.put("供应商代码", "purchasecode");
		titlechange.put("采购链接", "purchaseurl");
		titlechange.put("颜色标签", "color");
		titlechange.put("备注", "remark");
		titlechange.put("在途库存", "inbound");
		titlechange.put("库存", "qty");
		titlechange.put("是否归档", "isDelete");

		// 在索引0的位置创建行（最顶端的行）
		Row row = sheet.createRow(0);
		for (int i = 0; i < titlelist.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(titlelist.get(i));
		}

		for (int i = 0; i < list.size(); i++) {
			Row skurow = sheet.createRow(i + 1);
			Map<String, Object> skumap = list.get(i);
			for (int j = 0; j < titlelist.size(); j++) {
				Cell cell = skurow.createCell(j);
				if (skumap.get(titlechange.get(titlelist.get(j))) == null) {
					cell.setCellValue("-");
				} else {
					cell.setCellValue(skumap.get(titlechange.get(titlelist.get(j))).toString());
				}
			}
		}
	}

	public List<Map<String, Object>> selectCommonImage() {
		return this.baseMapper.selectCommonImage();
	}

	public List<Material> selectByImage(String image) {
		return this.baseMapper.selectByImage(image);
	}

	public Material selectBySKU(String shopid, String sku) {
		QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
		queryWrapper.eq("sku", sku);
		queryWrapper.eq("shopid", shopid);
		queryWrapper.eq("isDelete", false);
		List<Material> mlist = this.list(queryWrapper);
		if(mlist!=null && mlist.size()>0){
			return mlist.get(0);
		} 
		return null;
	}

	public List<MaterialConsumableVO> selectConsumableByMainmid(String id,String shopid) {
		List<MaterialConsumableVO> list=materialConsumableMapper.selectConsumableByMainMmid(id);
		if(list!=null && list.size()>0) {
			for(int i=0;i<list.size();i++) {
				String submaterialid = list.get(i).getId();
				Map<String, Object> invlist = inventoryMapper.findInvByWarehouseId(submaterialid, null, shopid);
				if (invlist!=null  && invlist.size()>0) {
					if(invlist.get("inbound")!=null) {
						list.get(i).setInbound(Integer.parseInt(invlist.get("inbound").toString()));
					}
					if(invlist.get("outbound")!=null) {
						list.get(i).setOutbound(Integer.parseInt(invlist.get("outbound").toString()));
					}
					if(invlist.get("fulfillable")!=null) {
						list.get(i).setFulfillable(Integer.parseInt(invlist.get("fulfillable").toString()));
					}
				}
			}
		}
		return list;
	}
	public List<MaterialConsumableVO> selectConsumableByMainmid(String id,String warehouseid ,String shopid) {
		List<MaterialConsumableVO> list=materialConsumableMapper.selectConsumableByMainMmid(id);
		if(list!=null && list.size()>0) {
			for(int i=0;i<list.size();i++) {
				String submaterialid = list.get(i).getId();
				Map<String, Object> invlist = inventoryMapper.findInvByWarehouseId(submaterialid, warehouseid, shopid);
				if(invlist.get("inbound")!=null) {
					list.get(i).setInbound(Integer.parseInt(invlist.get("inbound").toString()));
				}
				if(invlist.get("outbound")!=null) {
					list.get(i).setOutbound(Integer.parseInt(invlist.get("outbound").toString()));
				}
				if(invlist.get("fulfillable")!=null) {
					list.get(i).setFulfillable(Integer.parseInt(invlist.get("fulfillable").toString()));
				}
			}
		}
		return list;
	}
	public List<MaterialSupplierVO> selectSupplierByMainmid(String id) {
		List<MaterialSupplierVO> supplierList=materialSupplierMapper.selectSupplierByMainId(id);
		if(supplierList!=null && supplierList.size()>0) {
			for(MaterialSupplierVO item:supplierList) {
				String supid = item.getSupplierid();
				if(GeneralUtil.isNotEmpty(supid)) {
					List<MaterialSupplierStepwise> stepList=materialSupplierStepwiseMapper.selectSupplierByMainId(id,supid);
					if(stepList!=null && stepList.size()>0) {
						item.setStepList(stepList);
					}
				}
			}
			return supplierList;
		}else {
			return null;
		}
	}
	
	public List<Map<String, Object>> selectSupplerOtherById(String id) {
		List<Map<String, Object>> supplierList=materialSupplierMapper.selectSupplerOtherById(id);
		if(supplierList!=null && supplierList.size()>0) {
			for(Map<String, Object> item:supplierList) {
				String supid = item.get("supplierid").toString();
				if(GeneralUtil.isNotEmpty(supid)) {
					List<MaterialSupplierStepwise> stepList=materialSupplierStepwiseMapper.selectSupplierByMainId(id,supid);
					if(stepList!=null && stepList.size()>0) {
						item.put("stepList", stepList);
					}
				}
			}
			return supplierList;
		}else {
			return null;
		}
	}

	public List<MaterialSupplier> selectSupplierByMaterialId(String id) {
		QueryWrapper<MaterialSupplier> queryWrapper=new QueryWrapper<MaterialSupplier>();
		queryWrapper.eq("materialid",id);
		return  materialSupplierMapper.selectList(queryWrapper);
	}
	
	
	public int deleteMaterialSupplierStepwise(String materialid,String supplierid) {
		QueryWrapper<MaterialSupplierStepwise> queryWrapper=new QueryWrapper<MaterialSupplierStepwise>();
		queryWrapper.eq("materialid",materialid);
		queryWrapper.eq("supplierid",supplierid);
		return materialSupplierStepwiseMapper.delete(queryWrapper);
	}

	public int saveMaterialSupplierStepwise(MaterialSupplierStepwise mss) {
		// TODO Auto-generated method stub
	 	return materialSupplierStepwiseMapper.insert(mss);
	}

	public List<Map<String, Object>> findConsumableDetailByShipment(String shopid, String shipmentid ) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> result = materialConsumableMapper.findConsumableDetailByShipment( shopid,  shipmentid) ;
		LambdaQueryWrapper<OutWarehouseForm> query=new LambdaQueryWrapper<OutWarehouseForm>();
		query.eq(OutWarehouseForm::getShopid, shopid);
		query.eq(OutWarehouseForm::getNumber, shipmentid);
		OutWarehouseForm form=outWarehouseFormMapper.selectOne(query);
		Map<String,Integer> entryMap=new HashMap<String,Integer>();
		if(form!=null) {
			LambdaQueryWrapper<OutWarehouseFormEntry> queryEntry=new LambdaQueryWrapper<OutWarehouseFormEntry>();
			queryEntry.eq(OutWarehouseFormEntry::getFormid, form.getId());
			List<OutWarehouseFormEntry> list = outWarehouseFormEntryMapper.selectList(queryEntry);
			for(OutWarehouseFormEntry item:list) {
				entryMap.put(item.getMaterialid(), item.getAmount());
			}
			for(Map<String, Object> item:result) {
				item.put("out", entryMap.get(item.get("materialid").toString()));
			}
		}
		return result;
	}

	
  public 	List<Map<String, Object>> selectConsumableByMainSKU(String shopid,String warehouseid,List<Map<String,Object>> skulist){
	 if(skulist==null||skulist.size()==0) {return null;}
	 Map<String,Integer> mainSKUQty=new HashMap<String,Integer>();
	 for(Map<String, Object> item:skulist) {
         String sku=item.get("sku").toString();		 
         Integer qty = mainSKUQty.get(sku);
         if(qty==null) {
        	 qty=0;
         }
		qty=qty+Integer.parseInt(item.get("amount").toString());
		mainSKUQty.put(sku, qty);
	 }
	 Map<String,Map<String,Object>> consumableMap =new HashMap<String,Map<String,Object>>();
	 for(Entry<String, Integer> entry:mainSKUQty.entrySet()) {
		 String sku=entry.getKey();
	     Material mainMaterial = this.findBySKU(sku, shopid);
	     Integer mainPurchaseQty = mainSKUQty.get(sku);
		 List<Map<String, Object>> list = null;
		 this.selectConsumableByMainmid(mainMaterial.getId(),shopid);
		 Map<String, Object> mainmap = inventoryMapper.getSelfInvBySKU(warehouseid ,mainMaterial.getId());
		 mainmap.put("material", mainMaterial);
		 mainmap.put("mainPurchaseQty", mainPurchaseQty);
		 for(Map<String, Object> item:list) {
			 BigDecimal unitAmount=(BigDecimal) item.get("amount");
			 String key = item.get("id").toString();
			 Material submaterial = this.getById(key);
			 Map<String,Object> conitem = consumableMap.get(key);
			 if(conitem==null) {
				 conitem=new HashMap<String,Object>();
				 consumableMap.put(key, conitem);
			 }
			 Integer needpurchase =0;
			 List<Map<String,Object>> mainMap=null;
			 if(conitem.get("mainlist")!=null) {
				 mainMap = (List<Map<String,Object>>) conitem.get("mainlist");
			 }else {
				 mainMap=new ArrayList<Map<String,Object>>();
				 conitem.put("mainlist",mainMap);
			 }
			 mainmap.put("rate", unitAmount);
			 mainMap.add(mainmap);
			 conitem.put("mainlist",mainMap);
			 if(conitem.get("needpurchase")!=null) {
				 needpurchase = (Integer) conitem.get("needpurchase");
			 }
			 needpurchase=needpurchase+(int)Math.ceil(unitAmount.doubleValue()*(mainPurchaseQty*1.0));
			 conitem.put("needpurchase",needpurchase);
			 conitem.put("material", submaterial);
			 String supplierid = submaterial.getSupplier();
			 if(supplierid!=null) {
				 Customer supplier = customerService.getById(supplierid);
				 conitem.put("supplier",supplier);
			 }
			 Map<String, Object> map = inventoryMapper.getSelfInvBySKU(warehouseid,submaterial.getId());
			 conitem.putAll(map);
		 }
	 }
	 List<Map<String, Object>> result=new ArrayList<Map<String,Object>>();
	 result.addAll(consumableMap.values());
	 return  result;
	  
  }

public List<Map<String, Object>> findConsumableDetailList(Map<String, Object> maps) {
	return materialConsumableMapper.findConsumableDetailList(maps);
}

@Transactional
public int saveInventoryConsumable(UserInfo user,ConsumableOutFormDTO dto) {
    int result=0;
		String warehouseid=dto.getWarehouseid();
		if(StrUtil.isBlank(warehouseid)) {
			throw new BizException("出库仓库不能为空");
		}
		if(dto.getSkulist()!=null && dto.getSkulist().size()>0) {
			OutWarehouseForm form=new OutWarehouseForm();
			String bigid = warehouseService.getUUID();
			String formid = bigid;
			form.setNumber(dto.getShipmentid());
			form.setAudittime(new Date());
			form.setAuditstatus(2);
			form.setCreatedate(new Date());
			form.setCreator(user.getId());
			form.setWarehouseid(warehouseid);
			form.setShopid(user.getCompanyid());
			form.setOpttime(new Date());
			form.setOperator(user.getId());
			form.setRemark("发货单耗材出库");
			form.setId(formid);
			outWarehouseFormMapper.insert(form);
			for(int i=0;i<dto.getSkulist().size();i++) {
				InventoryParameter para = dto.getSkulist().get(i);
				int qty= para.getAmount();
				Material material = this.findBySKU(para.getSku(), user.getCompanyid());
				if(material!=null) {
					OutWarehouseFormEntry record=new OutWarehouseFormEntry();
					record.setAmount(qty);
					record.setFormid(formid);
					record.setMaterialid(material.getId());
					outWarehouseFormEntryMapper.insert(record);
					para.setAmount(qty);
					para.setFormid(formid);
					para.setFormtype("outstockform");
					para.setMaterial(material.getId());
					para.setNumber(dto.getShipmentid());
					para.setOperator(user.getId());
					para.setOpttime(new Date());
					para.setShopid(user.getCompanyid());
					para.setStatus(EnumByInventory.alReady);
					para.setWarehouse(warehouseid);
					result+= inventoryService.outStockByDirect(para);
				}
			}
		}
	return result;
}

public List<Map<String, Object>> findConsumableHistory(String shopid, String shipmentid) {
	QueryWrapper<OutWarehouseForm> queryWrapper=new QueryWrapper<OutWarehouseForm>();
	queryWrapper.eq("number",shipmentid);
	queryWrapper.eq("shopid",shopid);
	List<OutWarehouseForm> formlist = outWarehouseFormMapper.selectList(queryWrapper);
	if(formlist!=null && formlist.size()>0) {
		OutWarehouseForm form = formlist.get(0);
		String formid = form.getId();
		List<Map<String, Object>> list = outWarehouseFormEntryMapper.findFormEntryByFormid(formid);
		return list;
	}
	return null;
}

public List<Map<String, Object>> selectProPriceHisById(String id) {
	return erpMaterialHistoryMapper.selectProPriceHisById(id);
}
 
public MaterialCustoms selectCustomsByMaterialId(String id) {
	return materialCustomsMapper.selectById(id);
}

@Override
public Map<String, Object> updateMaterialCustoms(String id, String addfee, String material,String ftype) {
	MaterialCustoms cust = materialCustomsMapper.selectById(id);
	int res=0;
	if(cust!=null) {
		if("addfee".equals(ftype)) {
			if(GeneralUtil.isNotEmpty(addfee)) {
				cust.setAddfee(new BigDecimal(addfee));
			}else {
				cust.setAddfee(null);
			}
		}else {
			if(GeneralUtil.isNotEmpty(material)) {
				cust.setMaterial(material);
			}else {
				cust.setMaterial(null);
			}
		}
		res=materialCustomsMapper.updateById(cust);
	}
	Map<String,Object> maps=new HashMap<>();
	if(res>0) {
		maps.put("msg", "操作成功!");
		maps.put("code", "success");
	}else {
		maps.put("msg", "操作失败!");
		maps.put("code", "fail");
	}
	return maps;
}

public Map<String, Object> getRealityPrice(String materialid){
	return this.baseMapper.getRealityPrice(materialid);
}

	@Override
	public int uploadMaterialImg(UserInfo userinfo, String materialid, InputStream inputStream, String filename) {
		int result=0;
		Picture picture =null;
		try {
			if(StrUtil.isNotEmpty(filename)) {
				Material material = this.baseMapper.selectById(materialid);
				if(material!=null) {
					String filePath = PictureServiceImpl.materialImgPath + userinfo.getCompanyid();
					int len = filename.lastIndexOf(".");
					String filenames = filename.substring(0, len);
					String imgtype=filename.substring(len, filename.length());
					filename=filenames+System.currentTimeMillis()+imgtype;
					picture = pictureService.uploadPicture(inputStream, null, filePath, filename, material.getImage());
					if(picture!=null) {
						material.setImage(picture.getId());
						this.updateById(material);
						result=1;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Material> getMaterialByInfo(String shopid, String sku, String name) {
		QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
		queryWrapper.eq("shopid", shopid);
		queryWrapper.eq("isDelete", false);
		if(StrUtil.isNotEmpty(name)) {
			queryWrapper.like("name", "%"+name+"%");
		}
		if(StrUtil.isNotEmpty(sku)) {
			queryWrapper.eq("sku", sku);
		}
		List<Material> list = this.baseMapper.selectList(queryWrapper);
		if(list!=null && list.size()>0) {
			for(int i=0;i<list.size();i++) {
				String picid = list.get(i).getImage();
				Picture picture = pictureService.getById(picid);
				if(picture!=null && picture.getLocation()!=null) {
					String value=picture.getLocation();
					list.get(i).setImage(fileUpload.getPictureImage(value)); 
				}
			}
		}
		return list;
	}

	@Override
	public List<String> getTagsIdsListByMsku(String msku, String shopid) {
		List<String> tagids=null;
		QueryWrapper<Material> queryWrapper=new QueryWrapper<Material>();
		queryWrapper.eq("shopid", shopid);
		queryWrapper.eq("sku", msku);
		queryWrapper.eq("isDelete", 0);
		List<Material> mlist = this.baseMapper.selectList(queryWrapper);
		if(mlist!=null && mlist.size()>0) {
			String mid=mlist.get(0).getId();
			QueryWrapper<MaterialTags> tagWrapper=new QueryWrapper<MaterialTags>();
			tagWrapper.eq("mid", mid);
			List<MaterialTags> taglist = materialTagsMapper.selectList(tagWrapper);
			if(taglist!=null && taglist.size()>0) {
				tagids=new ArrayList<String>();
				for (int i = 0; i < taglist.size(); i++) {
					tagids.add(taglist.get(i).getTagid());
				}
			}
		}
		return tagids;
	}

	@Override
	public List<Map<String,Object>> saveTagsByMid(String mid, String tagids, String userid) {
		Set<String> tagsIdsList=new HashSet<String>();
		if(tagids.contains(",")) {
			//先删除老的 再save
			QueryWrapper<MaterialTags> queryWrapper=new QueryWrapper<MaterialTags>();
			queryWrapper.eq("mid", mid);
			materialTagsMapper.delete(queryWrapper);
			tagids=tagids.substring(0, tagids.length()-1);
			String[] tagsArray = tagids.split(",");
			for (int i = 0; i < tagsArray.length; i++) {
				String tagid = tagsArray[i];
				MaterialTags entity=new MaterialTags();
				entity.setMid(mid);
				entity.setOperator(userid);
				entity.setOpttime(new Date());
				entity.setTagid(tagid);
				materialTagsMapper.insert(entity);
				tagsIdsList.add(tagid);
			}
		}else {
			//清空了标签
			QueryWrapper<MaterialTags> queryWrapper=new QueryWrapper<MaterialTags>();
			queryWrapper.eq("mid", mid);
			materialTagsMapper.delete(queryWrapper);
		}
		Result<List<Map<String,Object>>> tagnamelistResult=adminClientOneFeign.findTagsNameByIds(tagsIdsList);
		List<Map<String,Object>> tagnamelist=tagnamelistResult.getData();
		return tagnamelist;
	}

	@Override
	public String findMaterialTagsByMid(String mid) {
		String strs="";
		QueryWrapper<MaterialTags> queryWrapper=new QueryWrapper<MaterialTags>();
		queryWrapper.eq("mid", mid);
		List<MaterialTags> list = materialTagsMapper.selectList(queryWrapper);
		if(list!=null && list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				MaterialTags item = list.get(i);
				strs+=(item.getTagid()+",");
			}
		}
		if(strs.contains(",")) {
			strs=strs.substring(0, strs.length()-1);
		}
		return strs;
	}

	@Override
	public List<MaterialCustomsItem> selectCustomsItemListById(String id) {
		QueryWrapper<MaterialCustomsItem> queryWrapper=new QueryWrapper<MaterialCustomsItem>();
		queryWrapper.eq("materialid", id);
		return materialCustomsItemMapper.selectList(queryWrapper);
	}

	@Override
	public List<Map<String, Object>> findInventoryByMsku(PlanDTO dto) {
		// TODO Auto-generated method stub
		if(StrUtil.isBlankOrUndefined(dto.getOwner())) {
			dto.setOwner(null);
		}
		if(StrUtil.isBlankOrUndefined(dto.getCategoryid())) {
			dto.setCategoryid(null);
		}
		if(StrUtil.isBlankOrUndefined(dto.getHasAddFee())) {
			dto.setHasAddFee(null);
		}
		return this.baseMapper.findInventoryByMsku(dto);
	}

	@Override
	public Material getBySku(String shopid, String sku) {
		// TODO Auto-generated method stub
		return  this.baseMapper.getMaterailBySku(shopid,sku);
		 
	}

	@Override
	public Map<String, String> getTagsIdsListByMsku(String shopid, List<String> mskulist) {
		Map<String, Object> param=new HashMap<String,Object>();
		param.put("shopid", shopid);
		param.put("skulist", mskulist);
		List<Map<String,String>> list=materialTagsMapper.getTagsBySku(param);
		Map<String,String> result=new HashMap<String,String>();
		for(Map<String,String> item:list) {
			result.put(item.get("sku"), item.get("tagids"));
		}
		return result;
	}

	@Override
	public void setMaterialExcelBook(Workbook workbook, MaterialDTO dto, UserInfo userinfo) {
		Map<String, Object> map = new HashMap<String, Object>();
    	String ftype= GeneralUtil.getValueWithoutBlank( dto.getFtype());
		String shopid = userinfo.getCompanyid();
		String search=dto.getSearch();
		String searchlist=dto.getSearchlist();
		String issfg=dto.getIssfg();
		String warehouseid = GeneralUtil.getValueWithoutBlank(dto.getWarehouseid());
		String isDelete = GeneralUtil.getValueWithoutBlank(dto.getIsDelete());
		String categoryid = GeneralUtil.getValueWithoutBlank(dto.getCategoryid());
		String supplier = GeneralUtil.getValueWithoutBlank(dto.getSupplier());
		String owner = GeneralUtil.getValueWithoutBlank(dto.getOwner());
		String name=GeneralUtil.getValueWithoutBlank(dto.getName()); 
		String remark= GeneralUtil.getValueWithoutBlank(dto.getRemark()); 
		String color=dto.getColor();
		String addressid= GeneralUtil.getValueWithoutBlank(dto.getAddressid());
		String materialid=GeneralUtil.getValueWithoutBlank(dto.getMaterialid());
		if (StrUtil.isBlankOrUndefined(search)) {
			search = null;
		} else {
			search = "%" + search.trim()+ "%";
		}

		String[] list = null;
		if (StrUtil.isNotEmpty(searchlist)) {
			list = searchlist.split(",");
		}
		List<String> skulist = new ArrayList<String>();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (StrUtil.isNotEmpty(list[i])) {
					skulist.add("%" + list[i] + "%");
				}
			}
		} else {
			skulist = null;
		}
	 
		if ( !StrUtil.isBlankOrUndefined(issfg) && issfg.equals("true")) {
			issfg = "2";
		} else if (StrUtil.isBlankOrUndefined(issfg) || issfg.equals("false")) {
			issfg = null;
		}
		List<Integer> issfglist = new ArrayList<Integer>();
		if (issfg != null) {
			String[] issfgarray = issfg.split(",");
			for (int i = 0; i < issfgarray.length; i++) {
				if (StrUtil.isNotEmpty(issfgarray[i])) {
					issfglist.add(Integer.parseInt(issfgarray[i]));
				}
			}
		} else {
			issfglist = null;
		}

		 
		if (StrUtil.isBlankOrUndefined(name)) {
			name = null;
		} else {
			name = "%" + name + "%";
		}
		 
		if (StrUtil.isBlankOrUndefined(remark)) {
			remark = null;
		} else {
			remark = "%" + remark + "%";
		}
	 
		if (StrUtil.isBlankOrUndefined(color) || "all".equals(color)) {
			color = null;
		} else {
			color = "%" + color.trim() + "%";
		}
		map.put("shopid", shopid);
		map.put("search", search);
		map.put("searchtype", dto.getSearchtype());
		map.put("searchsku", skulist);
		map.put("issfglist", issfglist);
		map.put("supplierid", supplier);
		map.put("categoryid", categoryid);
		map.put("name", name);
		map.put("remark", remark);
		map.put("owner", owner);
		map.put("color", color);
		map.put("warehouseid", warehouseid);
		map.put("ftype", ftype);
		map.put("isDelete", isDelete);
		map.put("addressid", addressid);
		map.put("materialid", materialid);
		List<Map<String, Object>> records = this.baseMapper.findMaterial(map);
		if(records!=null && records.size()>0) {
			Sheet sheet = workbook.getSheet("Sheet1");
			//处理组装关系数据
			if("MaterialAssembly".equals(dto.getDowntype())) {
				for(int i=0;i<records.size();i++) {
					Map<String, Object> item = records.get(i);
					String sku=item.get("sku").toString();
					String mid=item.get("id").toString();
					List<AssemblyVO> assemblyList = assemblyService.selectByMainmid(mid);
					Row row = sheet.createRow(i + 1);
					Cell cell = row.createCell(0); // 在索引0的位置创建单元格(左上端) {A001:2},{A002:1}
					cell.setCellValue(sku);
					Cell cell2 = row.createCell(1);
					String strs="";
					if(assemblyList!=null && assemblyList.size()>0) {
						for (int j = 0; j < assemblyList.size(); j++) {
							AssemblyVO assvo = assemblyList.get(j);
							strs+=("{"+assvo.getSku()+":"+assvo.getSubnumber()+("},"));
						}
					}else {
						strs="暂无";
					}
					cell2.setCellValue(strs);
				}
			}
			//处理海关关系数据
			if("MaterialCustoms".equals(dto.getDowntype())) {
				for(int i=0;i<records.size();i++) {
					Map<String, Object> item = records.get(i);
					String sku=item.get("sku").toString();
					String mid=item.get("id").toString();
					List<MaterialCustomsItem> customsItemList=this.selectCustomsItemListById(mid);
					MaterialCustoms Customs=this.selectCustomsByMaterialId(mid);
					Row row = sheet.createRow(i + 1);
					Cell cell = row.createCell(0); 
					cell.setCellValue(sku);
					if(Customs!=null) {
						Cell cell2 = row.createCell(1);
						cell2.setCellValue(Customs.getNameCn());
						Cell cell3 = row.createCell(2);
						cell3.setCellValue(Customs.getNameEn());
						Cell cell4 = row.createCell(3);
						cell4.setCellValue(Customs.getMaterial());
						Cell cell5 = row.createCell(4);
						cell5.setCellValue(Customs.getModel());
						Cell cell6 = row.createCell(5);
						cell6.setCellValue(Customs.getMaterialUse());
						Cell cell7 = row.createCell(6);
						cell7.setCellValue(Customs.getBrand());
						Cell cell8 = row.createCell(7);
						if(Customs.getIselectricity()==true) {
							cell8.setCellValue("是");
						}else {
							cell8.setCellValue("否");
						}
						Cell cell9 = row.createCell(8);
						if(Customs.getIsdanger()==true) {
							cell9.setCellValue("是");
						}else {
							cell9.setCellValue("否");
						}
						Cell cell10 = row.createCell(9);
						if(Customs.getUnitprice()!=null) {
							cell10.setCellValue(Customs.getUnitprice().toString());
						}
						Cell cell11 = row.createCell(10);
						if(Customs.getAddfee()!=null) {
							cell11.setCellValue(Customs.getAddfee().toString());
						}
					}
					Cell cell12 = row.createCell(11);
					String strs="";
					// {UK-12344AA-9.99-18} 国家-编码-费用-税率
					if(customsItemList!=null && customsItemList.size()>0) {
						for (int j = 0; j < customsItemList.size(); j++) {
							MaterialCustomsItem consitem = customsItemList.get(j);
							strs+=("{"+consitem.getCountry()+"-"+consitem.getCode()+"-"+consitem.getFee()+"-"+consitem.getTaxrate()+("},"));
						}
					}else {
						strs="";
					}
					cell12.setCellValue(strs);
				}
			}
			//处理基础数据
			if("MaterialBaseInfo".equals(dto.getDowntype())) {
				for(int i=0;i<records.size();i++) {
					Map<String, Object> item = records.get(i);
					String sku=item.get("sku").toString();
					String mid=item.get("id").toString();
					Material material = this.getById(mid); 
					Row row = sheet.createRow(i + 1);
					Cell cell = row.createCell(0); 
					cell.setCellValue(sku);
					if(material!=null) {
						Cell cell2 = row.createCell(1);
						cell2.setCellValue(material.getName());
						Cell cell3 = row.createCell(2);
						if(material.getPrice()!=null) {
							cell3.setCellValue(material.getPrice().toString());
						}
						if(StrUtil.isNotEmpty(material.getPkgdimensions()) ) {
							DimensionsInfo dim = dimensionsInfoService.getById(material.getPkgdimensions());
							if(dim!=null) {
								if(dim.getLength()!=null) {
									Cell cell4 = row.createCell(3);
									cell4.setCellValue(dim.getLength().toString());
								}
								if(dim.getWidth()!=null) {
									Cell cell5 = row.createCell(4);
									cell5.setCellValue(dim.getWidth().toString());
								}
								if(dim.getHeight()!=null) {
									Cell cell6 = row.createCell(5);
									cell6.setCellValue(dim.getHeight().toString());
								}
								if(dim.getWeight()!=null) {
									Cell cell7 = row.createCell(6);
									cell7.setCellValue(dim.getWeight().toString());
								}
							}
						}
						if(StrUtil.isNotEmpty(material.getItemdimensions()) ) {
							DimensionsInfo dim = dimensionsInfoService.getById(material.getItemdimensions());
							if(dim!=null) {
								if(dim.getLength()!=null) {
									Cell cell8 = row.createCell(7);
									cell8.setCellValue(dim.getLength().toString());
								}
								if(dim.getWidth()!=null) {
									Cell cell9 = row.createCell(8);
									cell9.setCellValue(dim.getWidth().toString());
								}
								if(dim.getHeight()!=null) {
									Cell cell10 = row.createCell(9);
									cell10.setCellValue(dim.getHeight().toString());
								}
								if(dim.getWeight()!=null) {
									Cell cell11 = row.createCell(10);
									cell11.setCellValue(dim.getWeight().toString());
								}
							}
						}
						if(StrUtil.isNotEmpty(material.getBoxdimensions()) ) {
							DimensionsInfo dim = dimensionsInfoService.getById(material.getBoxdimensions());
							if(dim!=null) {
								if(dim.getLength()!=null) {
									Cell cell12 = row.createCell(11);
									cell12.setCellValue(dim.getLength().toString());
								}
								if(dim.getWidth()!=null) {
									Cell cell13 = row.createCell(12);
									cell13.setCellValue(dim.getWidth().toString());
								}
								if(dim.getHeight()!=null) {
									Cell cell14 = row.createCell(13);
									cell14.setCellValue(dim.getHeight().toString());
								}
								if(dim.getWeight()!=null) {
									Cell cell15 = row.createCell(14);
									cell15.setCellValue(dim.getWeight().toString());
								}
							}
						}
						Cell cell16 = row.createCell(15);
						if(material.getBoxnum()!=null) {
							cell16.setCellValue(material.getBoxnum().toString());
						}
						Cell cell17 = row.createCell(16);
						if(material.getBrand()!=null) {
							MaterialBrand brandpojo = materialBrandMapper.selectById(material.getBrand());
							if(brandpojo!=null) {
								cell17.setCellValue(brandpojo.getName());
							}
						}
						Cell cell18 = row.createCell(17);
						if(material.getOtherCost()!=null) {
							cell18.setCellValue(material.getOtherCost().toString());
						}
						Cell cell19 = row.createCell(18);
						if(material.getCategoryid()!=null) {
							MaterialCategory catepojo = materialCategoryMapper.selectById(material.getCategoryid());
							if(catepojo!=null) {
								cell19.setCellValue(catepojo.getName());
							}
						}
						Cell cell20 = row.createCell(19);
						cell20.setCellValue(material.getRemark());
						Cell cell21 = row.createCell(20);
						cell21.setCellValue(GeneralUtil.formatDate(material.getEffectivedate(), "yyyy-MM-dd"));
					}
				}
			}
			//处理耗材关系数据
			if("MaterialConsumable".equals(dto.getDowntype())) {
				int rownum=1;
				for(int i=0;i<records.size();i++) {
					Map<String, Object> item = records.get(i);
					String sku=item.get("sku").toString();
					String mid=item.get("id").toString();
					List<MaterialConsumableVO> consumableList =  this.selectConsumableByMainmid(mid,shopid);
					Row row = sheet.createRow(rownum);
					Cell cell = row.createCell(0); 
					cell.setCellValue(sku);
					Cell cell2 = row.createCell(1);
					//当有耗材清单时 且清单>1 rownum要++
					if(consumableList!=null && consumableList.size()>0) {
						if(consumableList.size()<=1) {
							cell2.setCellValue(consumableList.get(0).getSku());
							Cell cell3 = row.createCell(2);
							cell3.setCellValue(consumableList.get(0).getAmount());
						}else {
							for (int j = 1; j < consumableList.size(); j++) {
								rownum++;
								MaterialConsumableVO vo = consumableList.get(j);
								Row conrow = sheet.createRow(rownum);
								Cell concell = conrow.createCell(0); 
								concell.setCellValue(sku);
								Cell concell2 = conrow.createCell(1); 
								concell2.setCellValue(vo.getSku());
								Cell concell3 = conrow.createCell(2); 
								concell3.setCellValue(vo.getAmount());
							}
						}
					}else {
						cell2.setCellValue("");
					}
					rownum++;
				}
			}
			//处理供应商关系数据
			if("MaterialSupplier".equals(dto.getDowntype())) {
				for(int i=0;i<records.size();i++) {
					Map<String, Object> item = records.get(i);
					String sku=item.get("sku").toString();
					String mid=item.get("id").toString();
					List<MaterialSupplierVO> lists =this.selectSupplierByMainmid(mid);
					MaterialSupplierVO listvo=null;
					if(lists!=null && lists.size()>0) {
						for (int k = 0; k < lists.size(); k++) {
							if(lists.get(k).getIsdefault()==true) {
								listvo=lists.get(k);
							}
						}
					}
					Row row = sheet.createRow(i + 1);
					Cell cell = row.createCell(0); // 在索引0的位置创建单元格(左上端) {100-9.9},{500-9.5}
					cell.setCellValue(sku);
					if(listvo==null) {
						continue;
					}
					Cell cell2 = row.createCell(1);
					cell2.setCellValue(listvo.getName());
					Cell cell3 = row.createCell(2);
					cell3.setCellValue(listvo.getProductCode());
					Cell cell4 = row.createCell(3);
					cell4.setCellValue(listvo.getPurchaseUrl());
					Cell cell5 = row.createCell(4);
					if(listvo.getOtherCost()!=null) {
						cell5.setCellValue(listvo.getOtherCost().toString());
					}
					Cell cell6 = row.createCell(5);
					cell6.setCellValue(listvo.getDeliverycycle());
					Cell cell7 = row.createCell(6);
					if(listvo.getBadrate()!=null) {
						cell7.setCellValue(listvo.getBadrate().toString());
					}
					Cell cell8 = row.createCell(7);
					cell8.setCellValue(listvo.getMOQ());
					Cell cell9 = row.createCell(8);
					String strs="";
					if(listvo.getStepList()!=null && listvo.getStepList().size()>0) {
						for (int j = 0; j < listvo.getStepList().size(); j++) {
							MaterialSupplierStepwise vo = listvo.getStepList().get(j);
							strs+=("{"+vo.getAmount()+"-"+vo.getPrice()+("},"));
						}
					}else {
						strs="无";
					}
					cell9.setCellValue(strs);
				}
			}
			
			
		}
		
	}

	@Override
	public Map<String,Object> getMaterialInfoBySkuList(PlanDTO dto){
		// TODO Auto-generated method stub
		 List<MaterialVO> list = this.baseMapper.getMaterialInfoBySkuList(dto);
		 Map<String,Object> result=new HashMap<String,Object>();
		 if(list!=null&&list.size()>0) {
			 for(MaterialVO item:list) {
				 result.put(item.getSku(), item);
			 }
		 }
		 return result;
	}


}
