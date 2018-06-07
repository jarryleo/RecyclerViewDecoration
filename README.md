# RecyclerViewDecoration
recyclerView条目悬浮吸顶控件

> 优势     
> 1：原生绘制悬浮头，顺滑流畅；内存占用极低，曲线平滑     
> 2：不影响悬浮头以及内部点击事件   
> 3：全程无入侵，300行代码的单个java文件   
> 缺点       
> 1：悬浮头的没有点击效果    

## Usage 

复制 [FloatDecoration.java](https://github.com/jarryleo/RecyclerViewDecoration/blob/master/app/src/main/java/cn/leo/recyclerviewdecoration/FloatDecoration.java)     

```
RecyclerView.addItemDecoration(new FloatDecoration(0));    
```

> 参数0 表示要悬浮的条目类型，跟adapter里面的getItemViewType值挂钩，可以多个
