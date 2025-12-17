package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 广告服务实现类
 * 实现广告的增删改查等功能
 *
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    AdvertisementRepository advertisementRepository;

    /**
     * 获取所有广告列表
     * @return 广告VO列表
     */
    @Override
    public List<AdvertisementVO> getAdvertisements() {
        List<Advertisement> advertisements = advertisementRepository.findAll();
        return advertisements.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private AdvertisementVO convertToVO(Advertisement advertisement) {
        return advertisement.toVO();
    }

    /**
     * 更新广告信息
     * @param advertisementVO 广告VO
     * @return 更新结果
     */
    @Override
    public String updateAdvertisement(AdvertisementVO advertisementVO) {
        Integer adId = advertisementVO.getId();
        System.out.println(adId);
        Optional<Advertisement> opAdvertisement = advertisementRepository.findById(advertisementVO.getId());
        if (!opAdvertisement.isPresent()){
            throw  TomatoMallException.AdvertisementNotExists();
        }
        Advertisement advertisement = opAdvertisement.get();
        String title = advertisementVO.getTitle();
        String content = advertisementVO.getContent();
        String imageUrl = advertisementVO.getImgUrl();
        Integer productId = advertisementVO.getProductId();
        if (productId == null){
            throw TomatoMallException.productNotExists();
        }
        advertisement.setProductId(productId);
        if (title != null){
            advertisement.setTitle(title);
        }
        if (content != null){
            advertisement.setContent(content);
        }
        if (imageUrl != null){
            advertisement.setImgUrl(imageUrl);
        }
        advertisementRepository.save(advertisement);

        return "更新成功";
    }

    /**
     * 创建广告
     * @param advertisementVO 广告VO
     * @return 新广告VO
     */
    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO) {
        Advertisement advertisement = advertisementVO.toPO();
        advertisementRepository.save(advertisement);
        return advertisement.toVO();
    }

    /**
     * 删除广告
     * @param advertisementId 广告ID
     * @return 删除结果
     */
    @Override
    public String deleteAdvertisement(Integer advertisementId) {
        Optional<Advertisement> opAdvertisement = advertisementRepository.findById(advertisementId);
        if (!opAdvertisement.isPresent()){
            throw  TomatoMallException.AdvertisementNotExists();
        }
        Advertisement advertisement = opAdvertisement.get();
        advertisementRepository.delete(advertisement);
        return "删除成功";
    }
}
