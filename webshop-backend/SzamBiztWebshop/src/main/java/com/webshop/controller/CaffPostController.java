package com.webshop.controller;

import com.webshop.model.CaffFile;
import com.webshop.model.CaffPost;
import com.webshop.repository.UserRepository;
import com.webshop.service.CaffFileService;
import com.webshop.service.CaffPostService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api")
public class CaffPostController {

    CaffPostService caffPostService;

    UserRepository userRepository;

    CaffFileService caffFileService;

    @Autowired
    public void setCaffPostService(CaffPostService caffPostService) {
        this.caffPostService = caffPostService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCaffFileService(CaffFileService caffFileService) {
        this.caffFileService = caffFileService;
    }

    @RequestMapping("/caff-posts/search")
    public ResponseEntity<List<CaffPost>> searchCaffByTitle(@RequestParam String title) {
        return new ResponseEntity<>(caffPostService.findCaffByTitle(title), HttpStatus.OK);
    }

    @RequestMapping("/caff-posts/all")
    public ResponseEntity<List<CaffPost>> getAllCaff() {
        return new ResponseEntity<>(caffPostService.getPosts(), HttpStatus.OK);
    }

    @RequestMapping(value = "/caff-posts/{id}", method = RequestMethod.GET)
    public ResponseEntity<CaffPost> findCaffById(@PathVariable long id) {
        return new ResponseEntity<>(caffPostService.findCaffById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/caff-posts/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteCaffById(@PathVariable long id) throws NotFoundException {
        caffPostService.deleteCaffById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/caff-posts/upload/{userId}", method = RequestMethod.POST)
    public ResponseEntity<Long> uploadCaff(
            @PathVariable long userId,
            @RequestBody byte[] uploadedByteArray
    ) throws IOException {
        CaffData result = readData(uploadedByteArray);

        BufferedImage image = new BufferedImage(
                (int) result.image_width,
                (int) result.image_height,
                BufferedImage.TYPE_INT_RGB
        );

        int z = 0;
        for (int y = 0; y < (int) result.image_height; y++) {
            for (int x = 0; x < (int) result.image_width; x++) {
                image.setRGB(x, y, result.pixels[z]);
                z++;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", baos);
        byte[] bytes = baos.toByteArray();

        CaffPost caffPost = new CaffPost();
        caffPost.setContent(bytes);
        caffPost.setPosted(new Date());

        if (userRepository.findById(userId).isPresent()) {
            caffPost.setUser(userRepository.findById(userId).get());
        }

        caffPost.setCreatorName(result.creator_name);
        caffPost.setCaption(result.caption);
        caffPost.setTags(result.tags);

        CaffPost savedCaffPost = caffPostService.uploadCaff(caffPost);

        this.caffFileService.saveCaffFile(savedCaffPost, uploadedByteArray);

        return new ResponseEntity<>(savedCaffPost.getId(), HttpStatus.OK);
    }

    @RequestMapping(value = "/caff-posts/upload-details/{caffPostId}", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> uploadCaff(
            @PathVariable long caffPostId,
            @RequestBody String title
    ) {
        CaffPost editableCaffPost = caffPostService.findCaffById(caffPostId);
        editableCaffPost.setTitle(title);
        caffPostService.updateTitle(editableCaffPost);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private native CaffData readData(byte[] file);
}
