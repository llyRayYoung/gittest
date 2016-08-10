package com.xyd.aike.gank.utils.imageutils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.xyd.aike.gank.utils.DensityUtil;
import com.xyd.aike.gank.utils.LogUtil;
import com.xyd.aike.gank.utils.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/2 0002.
 *
 * @desc 图片相关工具类
 */
public class ImageUtils {
    private Context context;
    private final String TAG = "ImageUtils";

    private ImageUtils(Context context) {
        this.context = context;
    }

    private volatile static ImageUtils instance;

    public static ImageUtils with(Context context) {
        if (instance == null) {
            synchronized (ImageUtils.class) {
                if (instance == null) {
                    instance = new ImageUtils(context);
                }
            }
        }
        return instance;
    }

    /**
     * 根据目标大小压缩图片
     * @param bitmap
     * @param target_size unit is kb
     * @return
     */
    public Bitmap compress(Context context, Bitmap bitmap, int target_size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int quality = 100;
        while (baos.toByteArray().length / 1024 > target_size && quality > 0) {
            baos.reset();
            quality -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        byte[] data = baos.toByteArray();
        int cp_length = data.length;
        LogUtil.i("compressed image length = " + cp_length / 1024 + " kb");
        return BitmapFactory.decodeByteArray(data, 0, cp_length);
    }

    /**
     * 计算图片的缩放值
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        } else {
            final int heightRatio = Math.round((float) reqHeight
                    / (float) height);
            final int widthRatio = Math.round((float) reqWidth / (float) width);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩图片大小
     *
     * @param context
     * @param bitmap
     */
    public void compress(Context context, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 80;//
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100 && options > 0) {
            baos.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public Bitmap getSmallBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
     * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
     * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    public Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        if (w < width) {
            width = w;
        }
        if (h < height) {
            height = h;
        }
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                    int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        System.out.println("w" + bitmap.getWidth());
        System.out.println("h" + bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 保存图片到本地
     *
     * @param filePath
     */
    public void saveImageToLocal(Context context, String filePath) {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            // 1.首先将文件保存到SD卡中。
            // String filePath = "xxx"; //全路径

            // saveImgToSDcard(filePath);
            // 2.增加Android 内部媒体索引。
            ContentValues values = new ContentValues();
            values.put("datetaken", new Date().toString());
            values.put("mime_type", "image/jpg");
            values.put("_data", filePath);
            ContentResolver cr = context.getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // 3.刷新filePath的上一级目录
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DCIM).getPath()
                            + "/"
                            + new File(filePath).getParentFile()
                            .getAbsolutePath()}, null, null);
            ToastUtil.showShort(context, "保存成功");
            // 发送广播，刷新相册
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(filePath));
            intent.setData(uri);
            context.sendBroadcast(intent);
        } else {
            ToastUtil.showLong(context, "SD卡不存在");
        }
    }

    /**
     * 放大或缩小图片
     *
     * @param bitmap 源图片
     * @param ratio  放大或缩小的倍数，大于1表示放大，小于1表示缩小
     * @return Bitmap
     */
    public Bitmap zoom(Bitmap bitmap, float ratio) {
        if (ratio < 0f) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 获取图片的旋转角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap 需要旋转的图片
     * @return 旋转后的图片
     */
    public Bitmap rotateBitmapByDegree(Bitmap bitmap, String path) {
        int degree = getBitmapDegree(path);
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return newBitmap;
    }


    // 图片展示相关的方法
    private int placeHolderImg; // 加载过程中的预览图片
    private int errorImg; // 加载失败展示的图片

    /**
     * 加载过程中的预览图片
     *
     * @param res
     * @return
     */
    public ImageUtils placeHolder(int res) {
        this.placeHolderImg = res;
        return this;
    }

    /**
     * 加载失败展示的图片
     *
     * @param res
     * @return
     */
    public ImageUtils error(int res) {
        this.errorImg = res;
        return this;
    }

    /**
     * 展示图片
     *
     * @param imageView 要展示图片的控件
     * @param url       图片的url
     */
    public void displayImage(final ImageView imageView, String url) {
//        DisplayWithGlide(imageView, url);
        DisplayWithImageLoader(imageView, url);
    }

    private void DisplayWithGlide(ImageView imageView, String url) {
        // 使用Glide
        Glide.with(context)
                .load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .fitCenter()
                .placeholder(placeHolderImg)
                .error(errorImg)
                .into(imageView);
    }

    private void DisplayWithImageLoader(final ImageView imageView, String url) {
        // 使用ImageLoader
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(placeHolderImg)
                .showImageForEmptyUri(errorImg)
                .showImageOnFail(errorImg)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().loadImage(
                url,
                options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        imageView.setImageBitmap(loadedImage);
                    }
                });
    }

    /**
     * 展示图片
     *
     * @param imageView      要展示图片的控件
     * @param url            图片的url
     * @param onLoadListener 加载成功的回调
     */
    public void displayImage(final ImageView imageView, String url, final OnLoadListener onLoadListener) {
//        DisplayWithGlideWithListener(url, onLoadListener);
        DisplayWithImageLoaderWithListener(url, onLoadListener);
    }

    private void DisplayWithImageLoaderWithListener(String url, final OnLoadListener onLoadListener) {
        // 使用ImageLoader
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(placeHolderImg)
                .showImageForEmptyUri(errorImg)
                .showImageOnFail(errorImg)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().loadImage(
                url,
                options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        onLoadListener.onFail();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        onLoadListener.onSuccess(loadedImage);
                    }
                });
    }

    private void DisplayWithGlideWithListener(String url, final OnLoadListener onLoadListener) {
        // 使用Glide
        Glide.with(context)
                .load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .fitCenter()
                .placeholder(placeHolderImg)
                .error(errorImg)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        Bitmap bitmap = drawable2Bitmap(resource);
                        if (bitmap == null) {
                            onLoadListener.onFail();
                        } else {
                            onLoadListener.onSuccess(bitmap);
                        }
                    }
                });
    }

    /**
     * 根据资源文件获取Bitmap对象
     *
     * @param resource 图片的资源id
     * @return
     */
    public Bitmap getBitmap(int resource) {
        Resources res = context.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, resource);
        return bmp;
    }

    /**
     * 根据Drawable对象获取Bitmap对象
     *
     * @param drawable Drawable对象
     * @return 返回Bitmap对象
     */
    public Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 根据Bitmap对象获取Drawable对象
     *
     * @param bitmap Bitmap对象
     * @return 返回Drawable对象
     */
    public Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    /**
     * 根据字节数组获取Bitmap对象
     *
     * @param b 字节数组
     * @return
     */
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0)
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        else
            return null;
    }

    /**
     * 根据Bitmap对象获取字节数组对象
     *
     * @param bitmap
     * @return
     */
    public byte[] Bimap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 在中间生成头像
     *
     * @param qr       二维码图片
     * @param protrait 头像
     * @return
     */
    public Bitmap createQrcodeWithProtrait(Bitmap qr, Bitmap protrait) {
        // 头像的大小
        int pWith = DensityUtil.dp2px(context, 40);
        // 二维码图片的大小
        int bitmapWidth = qr.getWidth();
        // 设置头像要显示的位置，即居中显示
        int left = (bitmapWidth - pWith) / 2;
        int top = (bitmapWidth - pWith) / 2;
        int right = left + pWith;
        int bottom = top + pWith;
        Rect rect = new Rect(left, top, right, bottom);
        // 取得qr二维码图片上的画笔，即要在二维码图片上绘制我们的头像
        Canvas canvas = new Canvas(qr);
        //画头像周围白边
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor("#ffffff"));
        // 为头像画底色
        canvas.drawRect(rect, paint);
        // 为头像画边框
//        //top
//        canvas.drawRect(left, top - 10, right + 10, top, paint);
//        //left
//        canvas.drawRect(left - 10, top - 10, left, bottom, paint);
//        //right
//        canvas.drawRect(right, top - 10, right + 10, bottom + 10, paint);
//        //bottom
//        canvas.drawRect(left - 10, bottom, right, bottom + 10, paint);
        canvas.drawBitmap(protrait, null, rect, null);
        return qr;
    }

}