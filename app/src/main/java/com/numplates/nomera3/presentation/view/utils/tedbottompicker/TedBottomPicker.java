package com.numplates.nomera3.presentation.view.utils.tedbottompicker;

import androidx.fragment.app.FragmentActivity;

@Deprecated
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

        public void showMultiImage(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            create().show(fragmentActivity.getSupportFragmentManager());
        }

        public void dismiss() {
            tedFragment.dismiss();
        }
    }

}
