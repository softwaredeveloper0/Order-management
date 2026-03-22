package com.example.productservice;

import lombok.Data;

@Data
public class ProductDTO {

    private long id;
    private String name;
    private String email;
    


    public void setId(Long id){
        this.id = id;
    }

    public Long getId(){
        return id;
    }

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   
   

    

}
