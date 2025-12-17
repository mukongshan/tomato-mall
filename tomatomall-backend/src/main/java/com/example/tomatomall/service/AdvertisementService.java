package com.example.tomatomall.service;

import com.example.tomatomall.vo.AdvertisementVO;

import java.util.List;

public interface AdvertisementService {
    List<AdvertisementVO> getAdvertisements();
    String updateAdvertisement(AdvertisementVO advertisementVO);
    AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO);
    String deleteAdvertisement(Integer advertisementId);
}
