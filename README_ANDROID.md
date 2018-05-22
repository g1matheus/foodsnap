
#1 Copy files - TensorFlowImageClassifier, Classifier, ClassifierActivity
   Copy folder - env helper folder - BorderedText, ImageUtils, Logger, Size, SplitTimer
   Copy assets - assets folder - graph.pb, labels.txt

------------------------------------------------------

#2 In build.gradle projects, add this
project.buildDir = 'gradleBuild'
getProject().setBuildDir('gradleBuild')

project.ext.ASSET_DIR = projectDir.toString() + '/app/assets'

assert file(project.ext.ASSET_DIR + "/graph.pb").exists()
assert file(project.ext.ASSET_DIR + "/labels.txt").exists()

------------------------------------------------------
#3 In build.gradle app, add this

dependencies {
    implementation 'org.tensorflow:tensorflow-android:+'
}

------------------------------------------------------

#4 Initialize Tensorflow Image Classifier
initTensorFlowAndLoadModel()

private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier =
                            TensorFlowImageClassifier.create(
                                    getAssets(),
                                    MODEL_FILE,
                                    LABEL_FILE,
                                    INPUT_SIZE,
                                    IMAGE_MEAN,
                                    IMAGE_STD,
                                    INPUT_NAME,
                                    OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
}

------------------------------------------------------

#5 Resize bitmap image and use in classifier
Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
txtPreview.setText(results.toString());