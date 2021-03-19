package com.example.streaming.contract;

import xyz.tanwb.airship.view.BasePresenter;
import xyz.tanwb.airship.view.BaseView;

public interface MainContract {

    interface View extends BaseView {

    }

    class Presenter extends BasePresenter<View> {

        @Override
        public void onStart() {
        }

    }
}
