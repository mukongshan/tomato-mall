package com.example.tomatomall.service.serviceImpl;

import com.alipay.api.domain.CodeCouponInfo;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.AccountCouponsRelationVO;
import com.example.tomatomall.vo.CouponVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    @Resource
    private CouponRepository couponRepository;

    @Resource
    private AccountCouponsRelationRepository accountCouponsRelationRepository;

    @Override
    public List<CouponVO> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        for (Coupon coupon : coupons){
            checkTime(coupon);
        }
        return coupons.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取账户的所有优惠券
     * @param accountId 账户ID
     * @return 账户优惠券列表
     */
    @Override
    public List<AccountCouponsRelationVO> getCouponsForAccount(Integer accountId) {
        // 1. 获取账户所有的优惠券关系记录
        List<AccountCouponsRelation> relations = accountCouponsRelationRepository.findByAccountId(accountId);

        // 4. 将优惠券实体转换为CouponVO
        return relations.stream()
                .map(this::convertToRelationVO)
                .collect(Collectors.toList());
    }

    private AccountCouponsRelationVO convertToRelationVO(AccountCouponsRelation po){
        AccountCouponsRelationVO vo = new AccountCouponsRelationVO();
        vo.setId(po.getId());
        vo.setAccountId(po.getAccountId());
        vo.setCouponId(po.getCouponId());
        vo.setQuantity(po.getQuantity());
        return vo;
    }

    private CouponVO convertToVO(Coupon coupon) {
        CouponVO couponVO = new CouponVO();
        couponVO.setId(coupon.getId());
        couponVO.setName(coupon.getName());
        couponVO.setDescription(coupon.getDescription());
        couponVO.setDiscountType(coupon.getDiscountType());
        couponVO.setDiscountValue(coupon.getDiscountValue());
        couponVO.setStartTime(coupon.getStartTime());
        couponVO.setEndTime(coupon.getEndTime());
        couponVO.setQuantity(coupon.getQuantity());
        couponVO.setUsedQuantity(coupon.getUsedQuantity());
        couponVO.setIsValid(coupon.getIsValid());
        return couponVO;
    }

    public CouponVO getCouponVOById(int couponId){
        Optional<Coupon> opCoupon = couponRepository.findById(couponId);
        if (!opCoupon.isPresent()){
            throw new TomatoMallException("找不到优惠券实体");
        }
        Coupon coupon = opCoupon.get();
        checkTime(coupon);
        return coupon.toVO();
    }

    /**
     * 发布优惠券（创建Coupon实体）
     * @param couponVO 优惠券VO
     * @return 发布结果
     */
    @Override
    @Transactional
    public String createCoupon(CouponVO couponVO) {
        Coupon coupon = couponVO.toPO();

        // 检查折扣值是否有效
        if (coupon.getDiscountType() == 1) { // 百分比折扣
            if (coupon.getDiscountValue() <= 0 || coupon.getDiscountValue() > 1) {
                throw new TomatoMallException("百分比折扣必须在0到1之间");
            }
        } else if (coupon.getDiscountType() == 2) { // 固定金额折扣
            if (coupon.getDiscountValue() <= 0) {
                throw new TomatoMallException("固定金额折扣必须大于0");
            }
        } else {
            throw new TomatoMallException("不支持的折扣类型");
        }

        // 检查时间是否有效
        if (coupon.getStartTime().isAfter(coupon.getEndTime())) {
            throw new TomatoMallException("开始时间不能晚于结束时间");
        }

        coupon.setIsValid(1);

        couponRepository.save(coupon);
        return "优惠券创建成功";
    }

    /**
     * 用户领取优惠券
     * @return 领取结果
     */
    @Transactional
    @Override
    public String userReceiveCoupon(AccountCouponsRelationVO accountCouponsRelationVO) {
        int accountId = accountCouponsRelationVO.getAccountId();
        int couponId = accountCouponsRelationVO.getCouponId();
        int quantity = accountCouponsRelationVO.getQuantity();

        // 检查优惠券是否存在
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new TomatoMallException("优惠券不存在"));

        // 检查优惠券是否可用
        if (coupon.getIsValid()!=1) {
            throw new TomatoMallException("优惠券不可用");
        }

        // 检查优惠券是否已过期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new TomatoMallException("优惠券已过期");
        }

        // 检查优惠券剩余数量
        if (coupon.getQuantity() - coupon.getUsedQuantity() < quantity) {
            throw new TomatoMallException("优惠券剩余数量不足");
        }


        // 查找用户是否已经拥有该优惠券
        AccountCouponsRelation pool = accountCouponsRelationRepository.findByAccountIdAndCouponId(accountId, couponId);

        if (pool!=null) {
            // 用户已拥有该优惠券，增加剩余数量
            addCouponAmountForAccount(accountId, couponId, quantity);
        } else {
            // 用户没有该优惠券，创建新记录
            AccountCouponsRelation newPool = new AccountCouponsRelation();
            newPool.setAccountId(accountId);
            newPool.setCouponId(couponId);
            newPool.setQuantity(quantity);
            accountCouponsRelationRepository.save(newPool);
        }

        // 减少Coupon中的总数量
        coupon.setUsedQuantity(coupon.getUsedQuantity() + quantity);
        couponRepository.save(coupon);

        return "领取成功，获得 " + quantity + " 张优惠券";
    }
    /**
     * 用户使用优惠券
     * @return 使用结果
     */
    @Transactional
    public String userUseCoupon(AccountCouponsRelationVO accountCouponsRelationVO) {
        int accountId = accountCouponsRelationVO.getAccountId();
        int couponId = accountCouponsRelationVO.getCouponId();
        int quantity = accountCouponsRelationVO.getQuantity();
        // 查找用户优惠券池中的记录
        AccountCouponsRelation pool = accountCouponsRelationRepository.findByAccountIdAndCouponId(accountId, couponId);
        if (pool==null){
            throw new TomatoMallException("用户没有该优惠券");
        }

        // 检查剩余数量是否足够
        if (pool.getQuantity() < quantity) {
            throw new TomatoMallException("优惠券剩余数量不足");
        }

        reduceCouponAmountForAccount(accountId, couponId, quantity);

        return "使用成功，剩余 " + pool.getQuantity() + " 张";
    }

    /**
     * 删除优惠券
     * @param couponId 优惠券ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public String deleteCoupon(int couponId) {
        // 1. 检查优惠券是否存在
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new TomatoMallException("优惠券不存在"));

        // 2. 删除优惠券
        couponRepository.delete(coupon);

        // 3. 删除与该优惠券相关的所有用户关系记录
        accountCouponsRelationRepository.deleteByCouponId(couponId);

        return "优惠券删除成功";
    }

    /**
     * 更新优惠券
     * @param couponVO 优惠券VO
     * @return 更新结果
     */
    @Override
    @Transactional
    public String updateCoupon(int id, CouponVO couponVO) {
        couponVO.setId(id);
        // 1. 检查优惠券是否存在
        Coupon coupon = couponRepository.findById(couponVO.getId())
                .orElseThrow(() -> new TomatoMallException("优惠券不存在"));

        // 2. 更新字段
        if (couponVO.getName() != null) coupon.setName(couponVO.getName());
        if (couponVO.getDescription() != null) coupon.setDescription(couponVO.getDescription());
        if (couponVO.getDiscountType() != null) coupon.setDiscountType(couponVO.getDiscountType());
        if (couponVO.getDiscountValue() != null) coupon.setDiscountValue(couponVO.getDiscountValue());
        if (couponVO.getStartTime() != null) coupon.setStartTime(couponVO.getStartTime());
        if (couponVO.getEndTime() != null) coupon.setEndTime(couponVO.getEndTime());
        if (couponVO.getQuantity() != null) coupon.setQuantity(couponVO.getQuantity());

        // 3. 再次验证
        if (coupon.getDiscountType() == 1) { // 百分比折扣
            if (coupon.getDiscountValue() <= 0 || coupon.getDiscountValue() > 1) {
                throw new TomatoMallException("百分比折扣必须在0到1之间");
            }
        } else if (coupon.getDiscountType() == 2) { // 固定金额折扣
            if (coupon.getDiscountValue() <= 0) {
                throw new TomatoMallException("固定金额折扣必须大于0");
            }
        }

        if (coupon.getStartTime().isAfter(coupon.getEndTime())) {
            throw new TomatoMallException("开始时间不能晚于结束时间");
        }

        // 4. 保存更新
        couponRepository.save(coupon);

        return "优惠券更新成功";
    }
    /**
     * 为特定账户增加优惠券剩余数量
     * @param couponId 优惠券ID
     * @param accountId 账户ID
     * @param amount 增加的数量
     * @return 操作结果
     */
    private String addCouponAmountForAccount(int accountId, int couponId, int amount) {
        // 1. 检查优惠券是否存在
        Optional<Coupon> opCoupon = couponRepository.findById(couponId);
        if (!opCoupon.isPresent()){
            throw new TomatoMallException("优惠券不存在");
        }
        Coupon coupon = opCoupon.get();

        // 2. 查找账户的优惠券关系记录
        AccountCouponsRelation relation = accountCouponsRelationRepository.findByAccountIdAndCouponId(accountId, couponId);
        if (relation==null) {
            throw  new TomatoMallException("用户没有该优惠券");
        }

        // 3. 增加剩余数量
        relation.setQuantity(relation.getQuantity() + amount);
        accountCouponsRelationRepository.save(relation);

        return "为账户增加优惠券数量成功，当前剩余数量：" + relation.getQuantity();
    }

    /**
     * 为特定账户减少优惠券剩余数量
     * @param couponId 优惠券ID
     * @param accountId 账户ID
     * @param amount 减少的数量
     * @return 操作结果
     */
    private String reduceCouponAmountForAccount(int accountId, int couponId, int amount) {
        // 1. 检查优惠券是否存在
        Optional<Coupon> opCoupon = couponRepository.findById(couponId);
        if (!opCoupon.isPresent()){
            throw new TomatoMallException("优惠券不存在");
        }
        Coupon coupon = opCoupon.get();

        // 2. 查找账户的优惠券关系记录
        AccountCouponsRelation relation = accountCouponsRelationRepository.findByAccountIdAndCouponId(accountId, couponId);
        if (relation==null) {
            throw  new TomatoMallException("用户没有该优惠券");
        }

        // 3. 检查减少后的数量是否合法
        if (relation.getQuantity() - amount < 0) {
            throw new TomatoMallException("优惠券剩余数量不能为负数");
        }

        // 4. 减少剩余数量
        relation.setQuantity(relation.getQuantity() - amount);
        accountCouponsRelationRepository.save(relation);

        return "为账户减少优惠券数量成功，当前剩余数量：" + relation.getQuantity();
    }

    /**
     * 检查优惠券是否在有效期内，如果过期则直接修改 isValid 字段
     * @param coupon 要检查的优惠券对象
     * @return 如果优惠券有效返回 true，如果过期返回 false（但 isValid 已被修改为 0）
     */
    private void checkTime(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(coupon.getEndTime())) { // 当前时间 > 结束时间
            coupon.setIsValid(0); // 设置为失效
        } else {
            coupon.setIsValid(1); // 保持有效
        }
        couponRepository.save(coupon);
    }
}