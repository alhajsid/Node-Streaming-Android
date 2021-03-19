package cn.nodemedia;

public interface NodePublisherVideoTextureDelegate {
    void onCreateTextureCallback(NodePublisher streamer);

    void onChangeTextureCallback(NodePublisher streamer, boolean isFront, int cameraOri, int windowOri);

    void onDestroyTextureCallback(NodePublisher streamer);

    int onDrawTextureCallback(NodePublisher streamer, int textureID, int width, int height, boolean isFront, int cameraOri);
}
