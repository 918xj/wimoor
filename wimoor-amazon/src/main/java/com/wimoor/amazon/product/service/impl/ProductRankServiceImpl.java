package com.wimoor.amazon.product.service.impl;

import com.wimoor.amazon.product.pojo.entity.ProductRank;
import com.wimoor.amazon.product.mapper.ProductRankMapper;
import com.wimoor.amazon.product.service.IProductRankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wimoor team
 * @since 2022-07-21
 */
@Service
public class ProductRankServiceImpl extends ServiceImpl<ProductRankMapper, ProductRank> implements IProductRankService {
   
	
	public int insert(ProductRank record){
		return this.baseMapper.insert(record);
	}
}
