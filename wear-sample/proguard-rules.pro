# Hibari inflates Views reflectively through a LayoutInflater factory, so constructors and
# LayoutParams must survive shrinking.
-keepclassmembers class * extends android.view.View {
    <init>(android.content.Context);
    <init>(android.content.Context, android.util.AttributeSet);
    public android.view.ViewGroup$LayoutParams generateDefaultLayoutParams();
}
-keepclassmembers class * extends android.view.ViewGroup$LayoutParams {
    <init>(int, int);
}
-keepnames class * extends android.view.View
-keepnames class *.*$LayoutParams
