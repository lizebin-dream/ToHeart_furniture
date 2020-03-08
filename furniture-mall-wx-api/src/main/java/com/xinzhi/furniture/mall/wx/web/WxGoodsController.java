package com.xinzhi.furniture.mall.wx.web;

import com.github.pagehelper.PageInfo;
import com.mysql.jdbc.StringUtils;
import com.xinzhi.furniture.mall.core.system.SystemConfig;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import com.xinzhi.furniture.mall.db.domain.*;
import com.xinzhi.furniture.mall.db.service.*;
import com.xinzhi.furniture.mall.db.service.resp.goods.GoodsOption;
import com.xinzhi.furniture.mall.wx.annotation.LoginUser;
import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.xinzhi.furniture.mall.core.validator.Order;
import com.xinzhi.furniture.mall.core.validator.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 商品服务
 */
@RestController
@RequestMapping("/wx/goods")
@Validated
public class WxGoodsController {
	private final Log logger = LogFactory.getLog(WxGoodsController.class);

	@Autowired
	private LitemallGoodsService goodsService;

	@Autowired
	private LitemallGoodsProductService productService;

	@Autowired
	private LitemallIssueService goodsIssueService;

	@Autowired
	private LitemallGoodsAttributeService goodsAttributeService;

	@Autowired
	private LitemallBrandService brandService;

	@Autowired
	private LitemallCommentService commentService;

	@Autowired
	private LitemallUserService userService;

	@Autowired
	private LitemallCollectService collectService;

	@Autowired
	private LitemallFootprintService footprintService;

	@Autowired
	private LitemallCategoryService categoryService;

	@Autowired
	private LitemallSearchHistoryService searchHistoryService;

	@Autowired
	private LitemallGoodsSpecificationService goodsSpecificationService;

	@Autowired
	private LitemallGrouponRulesService rulesService;

	@Autowired
	private LitemallGoodsOptionService litemallGoodsOptionService;

	private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

	private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(16, 16, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);

	/**
	 * 商品详情
	 * <p>
	 * 用户可以不登录。
	 * 如果用户登录，则记录用户足迹以及返回用户收藏信息。
	 *
	 * @param id     商品ID
	 * @return 商品详情
	 */
	@GetMapping("detail")
	public Object detail(@NotNull Integer id) {
		// 商品信息
		LitemallGoods info = goodsService.findById(id);

		// 商品属性
		Callable<List> goodsAttributeListCallable = () -> goodsAttributeService.queryByGid(id);

//		// 商品规格
		Callable<GoodsOption> optionCallable = () -> litemallGoodsOptionService.getLitemallGoodsOptionList(id);

		// 商品规格对应的数量和价格
		Callable<List> productListCallable = () -> productService.queryByGid(id);

		// 商品问题，这里是一些通用问题
		Callable<List> issueCallable = () -> goodsIssueService.querySelective("", 1, 4, "", "");

		FutureTask<List> goodsAttributeListTask = new FutureTask<>(goodsAttributeListCallable);
		FutureTask<GoodsOption> optionCallableTask = new FutureTask<>(optionCallable);
		FutureTask<List> productListCallableTask = new FutureTask<>(productListCallable);
		FutureTask<List> issueCallableTask = new FutureTask<>(issueCallable);

		executorService.submit(goodsAttributeListTask);
		executorService.submit(optionCallableTask);
		executorService.submit(productListCallableTask);
		executorService.submit(issueCallableTask);

		Map<String, Object> data = new HashMap<>();

		try {
			data.put("info", info);
			data.put("issue", issueCallableTask.get());
			data.put("option", optionCallableTask.get());
			data.put("productList", productListCallableTask.get());
			data.put("attribute", goodsAttributeListTask.get());
			data.put("share", SystemConfig.isAutoCreateShareImage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//商品分享图片地址
		data.put("shareImage", info.getShareUrl());
		return ResponseUtil.ok(data);
	}

	/**
	 * 商品分类类目
	 *
	 * @param id 分类类目ID
	 * @return 商品分类类目
	 */
	@GetMapping("category")
	public Object category(@NotNull Integer id) {
		LitemallCategory cur = categoryService.findById(id);
		LitemallCategory parent = null;
		List<LitemallCategory> children = null;

		if (cur.getPid() == 0) {
			parent = cur;
			children = categoryService.queryByPid(cur.getId());
			cur = children.size() > 0 ? children.get(0) : cur;
		} else {
			parent = categoryService.findById(cur.getPid());
			children = categoryService.queryByPid(cur.getPid());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("currentCategory", cur);
		data.put("parentCategory", parent);
		data.put("brotherCategory", children);
		return ResponseUtil.ok(data);
	}

	/**
	 * 根据条件搜素商品
	 * <p>
	 * 1. 这里的前五个参数都是可选的，甚至都是空
	 * 2. 用户是可选登录，如果登录，则记录用户的搜索关键字
	 *
	 * @param categoryId 分类类目ID，可选
	 * @param brandId    品牌商ID，可选
	 * @param keyword    关键字，可选
	 * @param isNew      是否新品，可选
	 * @param isHot      是否热买，可选
	 * @param page       分页页数
	 * @param limit       分页大小
	 * @param sort       排序方式，支持"add_time", "retail_price"或"name"
	 * @param order      排序类型，顺序或者降序
	 * @return 根据条件搜素的商品详情
	 */
	@GetMapping("list")
	public Object list(
		Integer categoryId,
		Integer brandId,
		String keyword,
		Boolean isNew,
		Boolean isHot,
		@RequestParam(defaultValue = "1") Integer page,
		@RequestParam(defaultValue = "10") Integer limit,
		@Sort(accepts = {"add_time", "retail_price", "name"}) @RequestParam(defaultValue = "add_time") String sort,
		@Order @RequestParam(defaultValue = "desc") String order) {

		//添加到搜索历史
//		if (userId != null && !StringUtils.isNullOrEmpty(keyword)) {
//			LitemallSearchHistory searchHistoryVo = new LitemallSearchHistory();
//			searchHistoryVo.setKeyword(keyword);
//			searchHistoryVo.setUserId(userId);
//			searchHistoryVo.setFrom("wx");
//			searchHistoryService.save(searchHistoryVo);
//		}

		//查询列表数据
		List<LitemallGoods> goodsList = goodsService.querySelective(categoryId, brandId, keyword, isHot, isNew, page, limit, sort, order);

		// 查询商品所属类目列表。
		List<Integer> goodsCatIds = goodsService.getCatIds(brandId, keyword, isHot, isNew);
		List<LitemallCategory> categoryList = null;
		if (goodsCatIds.size() != 0) {
			categoryList = categoryService.queryL2ByIds(goodsCatIds);
		} else {
			categoryList = new ArrayList<>(0);
		}

		PageInfo<LitemallGoods> pagedList = PageInfo.of(goodsList);

		Map<String, Object> entity = new HashMap<>();
		entity.put("list", goodsList);
		entity.put("total", pagedList.getTotal());
		entity.put("page", pagedList.getPageNum());
		entity.put("limit", pagedList.getPageSize());
		entity.put("pages", pagedList.getPages());
		entity.put("filterCategoryList", categoryList);

		// 因为这里需要返回额外的filterCategoryList参数，因此不能方便使用ResponseUtil.okList
		return ResponseUtil.ok(entity);
	}

	/**
	 * 商品详情页面“大家都在看”推荐商品
	 *
	 * @param id, 商品ID
	 * @return 商品详情页面推荐商品
	 */
	@GetMapping("related")
	public Object related(@NotNull Integer id) {
		LitemallGoods goods = goodsService.findById(id);
		if (goods == null) {
			return ResponseUtil.badArgumentValue();
		}

		// 目前的商品推荐算法仅仅是推荐同类目的其他商品
		int cid = goods.getCategoryId();

		// 查找六个相关商品
		int related = 6;
		List<LitemallGoods> goodsList = goodsService.queryByCategory(cid, 0, related);
		return ResponseUtil.okList(goodsList);
	}

	/**
	 * 在售的商品总数
	 *
	 * @return 在售的商品总数
	 */
	@GetMapping("count")
	public Object count() {
		Integer goodsCount = goodsService.queryOnSale();
		return ResponseUtil.ok(goodsCount);
	}

}