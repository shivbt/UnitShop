package com.ikai.unitshop;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by shiv on 2/12/17.
 */

public class BottomSheetForShopImage extends BottomSheetDialogFragment
        implements View.OnClickListener {

    private Communicator communicator;

    @Override
    public void setupDialog(Dialog dialog, int style) {

        super.setupDialog(dialog, style);

        // Get activity reference to Communicator object, so we can call communicate function
        // on this object.
        communicator = ((Communicator) getFragmentManager().findFragmentByTag("Home"));

        // Inflate view and set it to dialog.
        View contentView = View.inflate(getActivity(), R.layout.bottom_sheet_for_shop_image, null);
        dialog.setContentView(contentView);

        // Get reference for camera and gallery icon container.
        LinearLayout cameraIconContainer = contentView.findViewById(R.id.camera_container);
        LinearLayout galleryIconContainer = contentView.findViewById(R.id.gallery_container);

        // Set on click listener for both camera and gallery icon containers.
        cameraIconContainer.setOnClickListener(this);
        galleryIconContainer.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        // Check for camera click.
        if (id == R.id.camera_container) {
            // Call fragment implemented communicate() function with code 1.
            communicator.communicate(1);
            dismiss();
        }
        // Check for gallery click.
        if (id == R.id.gallery_container) {
            // Call fragment implemented communicate() function with code 2.
            communicator.communicate(2);
            dismiss();
        }

    }

    public interface Communicator {
        void communicate(int resultCode);
    }

}
