package com.droidmare.accounts.views.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.models.Multimedia;
import com.droidmare.accounts.utils.FileUtils;
import com.droidmare.accounts.views.activities.FilesActivity;

import java.util.ArrayList;

//Folders view adapter declaration
//@author Eduardo on 13/06/2019.

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private static ArrayList<Multimedia> fileList;

    private int focusedViewPosition;

    private FilesActivity filesActivity;

    // data is passed into the constructor
    public FilesAdapter(ArrayList<Multimedia> items, FilesActivity activity) {
        fileList = items;
        filesActivity = activity;
        focusedViewPosition = -1;
    }

    // inflates the grid element layout from xml when needed
    @Override @NonNull
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;

        //Depending on the file type, the layout used to inflate the view will be different:
        switch (viewType) {
            case FileUtils.FOLDER_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_folder, parent, false);
                break;
            case FileUtils.IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image, parent, false);
                break;
        }

        return new FileViewHolder(view);
    }


    // binds the data to the view in each grid element
    @Override
    public void onBindViewHolder(@NonNull final FileViewHolder holder, int position) {

        final Multimedia item = fileList.get(position);

        final boolean itemIsFolder = item.getType() == FileUtils.FOLDER_TYPE;

        // depending on the type of the file, the thumbnail is obtained and assigned in different ways:
        if (itemIsFolder) {
            if (item.getIcon() != null) holder.image.setImageBitmap(BitmapFactory.decodeFile(item.getIcon().getPath()));
            else holder.image.setImageDrawable(filesActivity.getDrawable(R.drawable.icon_default));
        }

        else {
            if (item.getBitmapIcon() == null) new ThumbnailTask(position, holder).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void) null);
            else holder.image.setImageBitmap(item.getBitmapIcon());
        }

        holder.name.setText(item.getName());

        holder.setBehaviour(item, itemIsFolder);
    }

    // returns the current focused grid item
    public int getFocusedPosition() { return focusedViewPosition; }

    // returns the number of items inside the grid
    @Override
    public int getItemCount() { return fileList.size(); }

    // stores and recycles views as they are scrolled off screen
    public class FileViewHolder extends RecyclerView.ViewHolder {

        public TextView name;

        ImageView image;

        FileViewHolder(View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.thumbnail);
            name = itemView.findViewById(R.id.nameFile);
        }

        // sets the behaviour of the focus and the click listener in each item:
        void setBehaviour (final Multimedia item, final boolean itemIsFolder) {
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {

                    if(hasFocus){
                        itemView.setAlpha(1f);
                        name.setTextColor(filesActivity.getResources().getColor(R.color.colorWhite));
                        focusedViewPosition = getAdapterPosition();
                        filesActivity.changeFileDescriptionText(name.getText().toString());
                    }

                    else{
                        itemView.setAlpha(0.5f);
                        name.setTextColor(filesActivity.getResources().getColor(R.color.black));
                        focusedViewPosition = -1;
                        filesActivity.changeFileDescriptionText("");
                    }
                }
            });

            // the behaviour of folders and images when they are clicked is different:
            if (itemIsFolder) itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String itemName = item.getName();
                    String itemPath = item.getPath();
                    int position = getAdapterPosition();

                    filesActivity.openFolder(itemName, itemPath, position);
                }
            });

            else itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filesActivity.sendPickedAvatar(item);
                }
            });
        }
    }

    // returns the type of the view (0 for folders and 1 for images)
    @Override
    public int getItemViewType(int position) {
        return fileList.get(position).getType();
    }

    // this class is used to obtain an image thumbnail (from the image itself), in the background, so the interface does not get freeze until all the thumbnails have been set:
    private static class ThumbnailTask extends AsyncTask<Void,Void,Bitmap> {

        private static final String TAG = ThumbnailTask.class.getCanonicalName();

        private final static int THUMB_SIZE = 200;

        private int mPosition;
        private FileViewHolder mHolder;

        ThumbnailTask(int position, FileViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            Bitmap thumb = null;

            try {
                Multimedia multimedia = fileList.get(mPosition);

                // since folders have a default thumbnail, only images' thumbnails must be generated:
                if (multimedia.getType() == FileUtils.IMAGE_TYPE) {
                    Bitmap test = getScaledBitmap(fileList.get(mPosition).getPath());
                    thumb = ThumbnailUtils.extractThumbnail(test, THUMB_SIZE, THUMB_SIZE);
                }

            } catch (RuntimeException rtException) {
                Log.e(TAG, "doInBackground. RuntimeException: " + rtException.getMessage());
            }

            return thumb;
        }

        // if the thumbnail´s size is bigger than 200pixels, it must be scaled:
        private Bitmap getScaledBitmap(String filePath) {

            BitmapFactory.Options options = new BitmapFactory.Options();

            int sampleSize = options.outWidth > THUMB_SIZE ? options.outWidth / THUMB_SIZE + 1 : 1;

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeFile(filePath, options);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            // once the thumbnail has been generated, it is assigned in the main thread:
            if (mPosition < fileList.size()) {

                fileList.get(mPosition).setBitmapIcon(bitmap);

                mHolder.image.setImageBitmap(bitmap);
            }
        }
    }
}
