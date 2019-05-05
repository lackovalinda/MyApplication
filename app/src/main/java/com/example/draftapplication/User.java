package com.example.draftapplication;

import java.io.Serializable;

public class User implements Serializable {

        private String username;
        private String email;
        private String id;
        private String image;

        public User() { }

        public User(String username, String email, String id, String image) {
            this.username = username;
            this.email = email;
            this.id = id;
            this.image = image;
        }

        public String getId() {
            return id;
        }

        public void setId(String id){
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {

            this.username = username;
        }

        public String getPhoto() {
            return image;
        }

        public void setPhoto(String image) {
            this.image = image;
        }

}
