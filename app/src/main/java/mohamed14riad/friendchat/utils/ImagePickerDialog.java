package mohamed14riad.friendchat.utils;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import mohamed14riad.friendchat.R;

import static android.app.Activity.RESULT_OK;

public class ImagePickerDialog extends DialogFragment implements View.OnClickListener {

    private LinearLayout openCameraItem = null;
    private LinearLayout openGalleryItem = null;

    private Uri imageUri = null;

    private Intent cameraIntent = null;
    private Intent galleryIntent = null;

    private static final int REQUEST_CAMERA = 111;
    private static final int REQUEST_GALLERY = 222;

    private static OnUriSelectListener uriSelectListener = null;

    public interface OnUriSelectListener {
        void onUriSelect(Uri imageUri);
    }

    public ImagePickerDialog() {

    }

    public static ImagePickerDialog newInstance(OnUriSelectListener selectListener) {
        ImagePickerDialog.uriSelectListener = selectListener;
        return new ImagePickerDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.image_picker_dialog, null);

        openCameraItem = (LinearLayout) dialogView.findViewById(R.id.openCameraItem);
        openCameraItem.setOnClickListener(this);

        openGalleryItem = (LinearLayout) dialogView.findViewById(R.id.openGalleryItem);
        openGalleryItem.setOnClickListener(this);

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(dialogView);
        dialog.setTitle(R.string.image_picker);
        dialog.show();
        return dialog;
    }

    @Override
    public void onClick(View item) {
        switch (item.getId()) {
            case R.id.openCameraItem:
                requestCameraPermission();
                break;
            case R.id.openGalleryItem:
                requestStoragePermission();
                break;
        }
    }

    private void requestCameraPermission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                        openCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            showSettingsDialog("Camera");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void requestStoragePermission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                        openStorage();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            showSettingsDialog("Storage");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog(String feature) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use " + feature + ". You can grant the permission in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 333);
    }

    private void openCamera() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void openStorage() {
        galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    imageUri = data.getData();
                    uriSelectListener.onUriSelect(imageUri);
                    dismiss();
                } else {
                    uriSelectListener.onUriSelect(null);
                    dismiss();
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    imageUri = data.getData();
                    uriSelectListener.onUriSelect(imageUri);
                    dismiss();
                } else {
                    uriSelectListener.onUriSelect(null);
                    dismiss();
                }
                break;
        }
    }
}
