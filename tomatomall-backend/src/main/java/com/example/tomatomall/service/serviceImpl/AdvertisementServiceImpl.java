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
import java.util.stream.Collectors;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    AdvertisementRepository advertisementRepository;

    @Override
    public List<AdvertisementVO> getAdvertisements() {
        List<Advertisement> advertisements = advertisementRepository.findAll();
        return advertisements.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private AdvertisementVO convertToVO(Advertisement advertisement) {
        return advertisement.toVO();
    }

    @Override
    public String updateAdvertisement(AdvertisementVO advertisementVO) {
        Integer adId = advertisementVO.getId();
        System.out.println(adId);
        Advertisement advertisement = advertisementRepository.findById(adId).get();
        if (advertisement == null){
            throw TomatoMallException.AdvertisementNotExists();
        }
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

    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO) {
        Advertisement advertisement = advertisementVO.toPO();
        advertisementRepository.save(advertisement);
        return advertisement.toVO();
    }

    @Override
    public String deleteAdvertisement(Integer advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId).get();
        if (advertisement == null){
            throw  TomatoMallException.AdvertisementNotExists();
        }
        advertisementRepository.delete(advertisement);
        return "删除成功";
    }
}
