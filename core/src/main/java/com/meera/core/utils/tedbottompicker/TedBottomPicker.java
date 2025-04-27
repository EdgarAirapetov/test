package com.meera.core.utils.tedbottompicker;

import androidx.fragment.app.FragmentActivity;


public class TedBottomPicker extends TedBottomSheetDialogFragment {

    public static Builder with(FragmentActivity fragmentActivity) {
        return new Builder(fragmentActivity);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private TedBottomSheetDialogFragment tedFragment;

        private Builder(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public TedBottomSheetDialogFragment show(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            tedFragment = create();
            tedFragment.show(fragmentActivity.getSupportFragmentManager());
            return tedFragment;
        }

        public TedBottomSheetDialogFragment showMultiImage(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            TedBottomSheetDialogFragment tedFragment = create();
            tedFragment.show(fragmentActivity.getSupportFragmentManager());
            return tedFragment;
        }

        public TedBottomSheetDialogFragment showMultiMediaPicker(OnMultiMediaSelectedListener onMultiMediaSelectedListener) {
            this.onMultiMediaSelectedListener = onMultiMediaSelectedListener;
            TedBottomSheetDialogFragment tedFragment = create();
            tedFragment.show(fragmentActivity.getSupportFragmentManager());
            return tedFragment;
        }

        public void dismiss() {
            tedFragment.dismiss();
        }
    }

}
