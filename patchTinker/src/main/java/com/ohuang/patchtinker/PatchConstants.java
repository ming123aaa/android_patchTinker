package com.ohuang.patchtinker;

 class PatchConstants {
     private String name;

      PatchConstants(String name) {
         this.name = name;
     }

     String SP_KEY_version() {
         return "version" + this.name;
     }

     String SP_KEY_isV2Patch() {
         return "isV2Patch" + this.name;
     }

     String SP_KEY_resEnable() {
         return "resEnable" + this.name;
     }

     String SP_KEY_installInfo() {
         return "installInfo" + this.name;
     }

     String rootPath() {
         return "/ohPatch" + this.name;
     }

     String dexPath() {
         return rootPath() + "/dex.apk";
     }

     String libPath() {
         return rootPath() + "/lib";
     }
}
