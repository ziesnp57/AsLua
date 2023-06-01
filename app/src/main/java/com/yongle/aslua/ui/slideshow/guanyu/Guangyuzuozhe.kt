package com.yongle.aslua.ui.slideshow.guanyu

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityGuangyuzuozheBinding

class Guangyuzuozhe : AppCompatActivity() {

    // 声明变量
    private var binding: ActivityGuangyuzuozheBinding? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.guanyuzuozhe)


        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityGuangyuzuozheBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding!!.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT

        //设置头像
        Glide.with(this).load("http://q1.qlogo.cn/g?b=qq&nk=2063809513&s=100").into(binding!!.imageView)

        binding!!.textView12.text = "歪果桃"

        binding!!.textView13.text = "努力为大家创造快乐!"

        binding!!.textView1.text = "　 Hi，我是本APP开发者歪果桃\n\n     QQ：2063809513\n\n    从始至今已经是我开发软件第三年，这三年来有太多太多的酸甜苦辣咸，回首过去，是幅壮丽景色，内心激动不已。\n 　我自高中时，开始踏进编程的世界，我对这个深有兴趣，甚是狂热。我对其他诸如游戏小说、二次元不怎么抱有兴趣，唯独对编程接近疯狂。在我看来，它太酷了，我是一个创造者，创造着我所理想的事物，也可以来控制它的全部，比如体验、界面等，甚至可以分享给其他人。我可以发泄，可以表露出我的特殊情感，它太神奇了。最开始接触编程语言是PHP,那时我还什么也不会,因为他是网站开发的编程语言所以上手也非常的快。在网上搜寻了有关它的教程,开始尝试写自己的第一个网站。打进第一个网站的那一刻我的内心十分激动,为了提升自己的技术，我开始在网上找各种教程。过程中难免也会遇到许多的问题,但我没有因为这些问题而半途而废。我加了有关这方面的群,在与他们交流的过程中我也学到了很多,认识了很多的人。大学生、和我同年级的、比我小些的弟弟妹妹。有些人是主动来和我交流，他们给了我很多的启发、鼓励与支持。有时候的悲伤，他们会帮我解忧，有时候的兴奋，他们同我一起品。我很难表达对他们的感激，他们给了太多太多的支持，不然我真就无法坚持。我也结交到了许多的朋友,我们一起交流一起进步，这让我在编程件中找到了快乐。\n 　 我开始用心写的Lua+助手(我也不知道为什么当时要取这么沙雕的名称)从开始的设计UI到添加功能,前前后后也花了很多的时间。开发出第一个版本后，我就拿给一些朋友使用，他们说没什么用，我很难过,其实根本就是他们体验不到位。但我没在意了，继续以高速度进行迭代更新。或许真的是因为没什么用吧,发布了好几个版本,用户还是少的可怜,群里一直就是十几二十个人。我的心情跌入低谷,每天聊天的基本是我。头几年，我一直抱着很大的兴趣与热情在进行学习与开发。我没有因此轻言放弃,不断的完善和优化。正是因为如此,功夫不负有心人。后面几个月用户的突增，群成员的增加，一次又一次的让我感到兴奋，有时候甚至想哭。工具箱的不易大多人是无法知道的，但不能怪他们，因为我觉得我技术还是特别差劲的，至少还没达到我理想的样子，但还好，进步空间很大。但是好景不长，或许是因为lua底层的问题吧,后来通过朋友的介绍。我认识了一款以Lua语言为主的编程软件AndroLua+,Lua是一种轻量小巧的脚本语言，用标准C语言编写并以源代码形式开放，其设计目的是为了嵌入应用程序中，从而为应用程序提供灵活的扩展和定制功能。正是因为如此,以至于他的大小只有2MB左右。麻雀虽小五脏俱全,他内置非常多的功能,我打算用Lua编写应用。因为是新的编程语言,语法也和之前有很大的区别，所以我又投入了很多时间在学习这门语言上。我阅读了数已百计的开源项目,我学习他们的写法。我不断的完善UI和功能,用我软件的用户也越来越多,在此期间还有许多人给我打赏,我也非常的感激他们。\n 希望以后推出的软件能给大家带来快乐,感谢一路陪伴。\n\n 2023.05.08\n\n 歪果桃\n"


    }


    // 设置返回按钮的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}