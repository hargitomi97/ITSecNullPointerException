package com.webshop.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.webshop.controller.CaffPostController;
import com.webshop.model.CaffPost;
import com.webshop.service.CaffPostService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = CaffPostController.class)
@ActiveProfiles("test")
public class CaffPostControllerTest {
	
	@Autowired                           
    private MockMvc mockMvc;  
                                                 
    @MockBean                           
    private CaffPostService caffPostService; 
                                       
                                            
    @BeforeEach                           
    void setUp() {                                                     
 
    }
    
    @Test
    void testFindCaffById() throws Exception{
    	final long caffId = 1L;
    	final CaffPost caffPost = new CaffPost();
    	
    	given(caffPostService.findCaffById(caffId)).willReturn(caffPost);
    	
    	this.mockMvc.perform(get("/api/caff-posts/{id}", caffId))
    		.andExpect(status().isOk())
    		.andExpect(jsonPath("$.title", is(caffPost.getTitle())))
    		.andExpect(jsonPath("$.creatorName", is(caffPost.creatorName)));
    }
    
}
