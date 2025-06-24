package com.example.myandroid.video.keyframe.video3;

/**
 * @author pxp
 * @description
 */
public class KFMediaBase {
    public enum KFMediaType {
        KFMediaUnkown(0),
        KFMediaAudio(1 << 0),
        KFMediaVideo(1 << 1),
        KFMediaAV((1 << 0) | (1 << 1));
        private int index;

        KFMediaType(int index) {
            this.index = index;
        }

        public int value() {
            return index;
        }
    }
}
