// MusicInterface.aidl
package com.project.hoangminh.service.audiocommon;

interface MusicInterface {

    //Define methods of the interface
    void play(String s);
    void pause();
    void resume();
    void stop();
    String getData();
}