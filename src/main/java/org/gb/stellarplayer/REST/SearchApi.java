package org.gb.stellarplayer.REST;

import org.gb.stellarplayer.Response.SearchResultDTO;
import org.gb.stellarplayer.Service.SearchService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchApi {
    @Autowired
    private SearchService searchService;
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<SearchResultDTO> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "5") int limit)
             {

        // Perform search
        SearchResultDTO results = searchService.search(query, limit);
        return ResponseEntity.ok(results);
    }
}
