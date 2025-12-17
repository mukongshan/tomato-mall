package com.example.tomatomall.controller;

import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AdvertisementVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Resource
    private AdvertisementService advertisementService;

    @GetMapping
    public Response<List<AdvertisementVO>> getAdvertisements() {
        return Response.buildSuccess(advertisementService.getAdvertisements());
    }


    @PostMapping
    public Response<AdvertisementVO> createAdvertisement(@RequestBody AdvertisementVO advertisementVO) {
        return Response.buildSuccess(advertisementService.createAdvertisement(advertisementVO));
    }


    @PutMapping
    public Response<String> updateAccount(@RequestBody AdvertisementVO advertisementVO) {
        return Response.buildSuccess(advertisementService.updateAdvertisement(advertisementVO));
    }

    @DeleteMapping("/{advertisementId}")
    public Response<String> upload(@PathVariable Integer advertisementId){
        return Response.buildSuccess(advertisementService.deleteAdvertisement(advertisementId));
    }
}