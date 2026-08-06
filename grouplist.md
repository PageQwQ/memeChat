文档根据memegroup.md扩展

# grouplist.txt

## 材质包memegroup组别名字显示

由于mojang对资源包的硬性限制，只能使用部分字符作为目录格式，特对此扩展

在原有的目录下新增

```
材质包名字/
	pack.mcmeta
	pack.png
	assets/
		memechat/
			memes/
				grouplist.txt（新增）
				memegroup/
					examplememe.png
					examplememe2.png
				group2/
					aaaa.gif
```

在 `grouplist.txt `中可以定义memegroup在分组组件的显示名字

`grouplist.txt` 的示例如下：

```
memegroup/ == "自定义名字",
group2/ == "组别2"
```

也就是其基本语法如下：

```
<目录名字>/ == "<映射名字>"
```

其中每一条目要用 `,` 并换行分隔