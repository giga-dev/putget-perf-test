package com.gigaspaces.app;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

@SpaceClass
public class KeyValEntry {

   public Long key;
   private String val;

   @SpaceRouting
   @SpaceId(autoGenerate = false)
   public Long getKey() {
      return key;
   }

   public void setKey(Long key) {
      this.key = key;
   }

   public String getVal() {
      return val;
   }

   public void setVal(String val) {
      this.val = val;
   }
}
