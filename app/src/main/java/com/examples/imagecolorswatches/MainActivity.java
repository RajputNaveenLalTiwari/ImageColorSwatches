package com.examples.imagecolorswatches;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "MainActivity";
    private static final int START_CAMERA_APP = 1;
    private static final int START_GALLERY_APP = 2;
    private static final int START_OPEN_DOCUMENT_APP = 3;

    private Button camera,gallery,openDocument;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera          = (Button) findViewById(R.id.camera);
        gallery         = (Button) findViewById(R.id.gallery);
        openDocument    = (Button) findViewById(R.id.openDocument);
        imageView       = (ImageView) findViewById(R.id.image);

        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        openDocument.setOnClickListener(this);
    }

    public void createPalette(Object object)
    {
        Bitmap bitmap;
        try
        {
            if(object instanceof Uri)
            {
                Uri imageUri = (Uri) object;
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                bitmap = (Bitmap) object;
                imageView.setImageBitmap(bitmap);
            }

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener()
            {
                @Override
                public void onGenerated(Palette palette)
                {
                    HashMap<String,Integer> hashMap = processPalette(palette);
                    Object[] entries = hashMap.entrySet().toArray();


                    for(Map.Entry entry:hashMap.entrySet())
                    {
                        Log.i("Key"+entry.getKey(),"Value"+entry.getValue());
                    }
                }
            });
        }
        catch (Exception e)
        {

        }
    }

    HashMap<String,Integer> processPalette(Palette palette)
    {
        HashMap<String,Integer> hashMap = new HashMap<>();

        if( palette.getVibrantSwatch() != null )
        {
            hashMap.put("VibrantSwatch",palette.getVibrantSwatch().getRgb());
        }

        if( palette.getDarkVibrantSwatch() != null )
        {
            hashMap.put("DarkVibrantSwatch",palette.getDarkVibrantSwatch().getRgb());
        }

        if( palette.getLightVibrantSwatch() != null )
        {
            hashMap.put("LightVibrantSwatch",palette.getLightVibrantSwatch().getRgb());
        }

        if( palette.getMutedSwatch() != null )
        {
            hashMap.put("MutedSwatch",palette.getMutedSwatch().getRgb());
        }

        if( palette.getDarkMutedSwatch() != null )
        {
            hashMap.put("DarkMutedSwatch",palette.getDarkMutedSwatch().getRgb());
        }

        if( palette.getLightMutedSwatch() != null)
        {
            hashMap.put("LightMutedSwatch",palette.getLightMutedSwatch().getRgb());
        }

        return hashMap;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.camera:
                callCameraApplication();
                break;
            case R.id.gallery:
                callGalleryApplication();
                break;
            case R.id.openDocument:
                callOpenDocumentApplication();
                break;

        }
    }

    private void callCameraApplication()
    {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,START_CAMERA_APP);
    }

    private void callGalleryApplication()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,START_GALLERY_APP);
    }

    private void callOpenDocumentApplication()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,START_OPEN_DOCUMENT_APP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode==RESULT_OK)
        {
            if(requestCode == START_CAMERA_APP && data != null)
            {
                Object object = data.getExtras().get("data");
                createPalette(object);
            }
            else if(requestCode == START_GALLERY_APP && data != null)
            {
                Object object = data.getData();
                createPalette(object);
            }
            else if(requestCode == START_OPEN_DOCUMENT_APP && data != null)
            {
                Uri imageUri = data.getData();
                createPalette(imageUri);
            }
            else
            {
                Log.e(TAG,"Something while opening camera/gallery/open_document");
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri ) throws IOException
    {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }
}
