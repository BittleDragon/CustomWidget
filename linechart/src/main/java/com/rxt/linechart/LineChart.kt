package com.rxt.linechart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

/**
 * Desc:折线图
 * Company: xuehai
 * Copyright: Copyright (c) 2019
 *
 * @author raoxuting
 * @since 2019/07/29 15/50
 */
class LineChart : View{

    /**
     * 线条画笔
     */
    private val linePaint: Paint = Paint()
    /**
     * 小圆点画笔
     */
    private val pointPaint: Paint = Paint()
    /**
     * 大圆点画笔
     */
    private val bigPointPaint: Paint = Paint()
    /**
     * 文字画笔
     */
    private val textPaint: Paint = Paint()
    /**
     * 排名数据
     */
    private var mRankList: MutableList<Int>? = null
    /**
     * 点集合
     */
    private var pointsList: MutableList<Point> = mutableListOf()
    /**
     * 最大圆点半径
     */
    private var maxPointRadius: Int
    /**
     * 圆点绘制的参数
     */
    private var pointsFloatArray: FloatArray? = null
    /**
     * 线条绘制参数
     */
    private var lineFloatArray: FloatArray? = null
    /**
     * 小圆点半径
     */
    private var smallPointsRadius = 6f
    /**
     * 大圆点半径
     */
    private var bigPointRadius = 12f
    /**
     * 大圆点描边宽度
     */
    private var bigPointStrokeWidth = 2f
    /**
     * 文字与圆点距离
     */
    private var textMargin = 4
    /**
     * 圆点可绘制区域高度
     */
    private var maxHeight: Int = 0
    /**
     * 排名文字竖直方向的padding
     */
    private val textPaddingVertical: Float
    /**
     * 排名文字水平方向的padding
     */
    private val textPaddingHorizontal: Float
    /**
     * 文字背景绘制区域
     */
    private var textBgRectF = RectF()
    /**
     * 文字背景圆角
     */
    private val textBgRectRadius: Float

    private val dp4: Int

    private var originLayer: Int? = null

    constructor(ctx: Context) : this(ctx, null)

    constructor(ctx: Context, attr: AttributeSet?) : this(ctx, attr, 0)

    constructor(ctx: Context, attr: AttributeSet?, defStyleAttr: Int) : super(ctx, attr, defStyleAttr) {
        smallPointsRadius = 6f
        bigPointRadius = 12f
        bigPointStrokeWidth = 2f
        textMargin = 16
        textPaddingVertical = 5f
        textPaddingHorizontal = 10f
        textBgRectRadius = 14f
        dp4 = 4

        linePaint.color = Color.parseColor("#d6e6ff")
        linePaint.strokeWidth = smallPointsRadius
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.FILL

        pointPaint.color = Color.parseColor("#6c9fff")
        //设置为圆角，以绘制圆点
        pointPaint.strokeCap = Paint.Cap.ROUND
        pointPaint.strokeWidth = smallPointsRadius * 2f
        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL

        bigPointPaint.isAntiAlias = true

        textPaint.color = Color.WHITE
        textPaint.textSize = 16f
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL

        maxPointRadius = bigPointRadius.toInt() + 4
    }

    /**
     * 设置排名
     */
    fun setRankList(rankList: MutableList<Int>) {
        if (rankList.isNullOrEmpty()) {
            mRankList?.clear()
            invalidate()
            return
        }
        this.mRankList = rankList
        val textCloseLeftEdge = "第${rankList.first()}名"
        val leftMargin = textPaint.measureText(textCloseLeftEdge) / 2 + textPaddingHorizontal + 1
        val textCloseRightEdge = "第${rankList.last()}名"
        val rightMargin = textPaint.measureText(textCloseRightEdge) / 2 + textPaddingHorizontal + 1
        //圆点可绘制区域宽度
        val maxWidth = width - paddingLeft - paddingRight - leftMargin - rightMargin
        //圆点可绘制区域高度
        maxHeight = height - paddingTop - paddingBottom - 2 * maxPointRadius - dp4 * 2
        //计算所有点的坐标
        pointsList.clear()
        if (rankList.size == 1) {
            pointsList.add(Point(1 + leftMargin.toInt() + maxWidth.toInt() / 2, maxPointRadius + maxHeight / 2 + 1))
        } else {
            //计算所有横坐标
            val deltaX = maxWidth / (rankList.size - 1)
            val pointXList: MutableList<Int> = mutableListOf()
            for (index in rankList.indices) {
                val pointX = leftMargin + index * deltaX
                pointXList.add(pointX.toInt())
            }

            //计算所有纵坐标
            val sortedDescendingList = rankList.sortedDescending()
            val maxRankRange = sortedDescendingList.first() - sortedDescendingList.last()
            if (maxRankRange == 0) {
                //生成所有点的坐标
                for (i in rankList.indices) {
                    pointsList.add(Point(pointXList[i], maxPointRadius + maxHeight / 2 + 1))
                }
            } else {
                val deltaY = maxHeight.toFloat()/ maxRankRange
                //最低名次--值最大的
                val minRank = sortedDescendingList.first()
                val pointYList : MutableList<Int> = mutableListOf()
                for (i in rankList) {
                    val pointY = maxPointRadius + dp4 + (maxHeight - Math.abs(i - minRank) * deltaY)
                    pointYList.add(pointY.roundToInt())
                }

                //生成所有点的坐标
                for (i in rankList.indices) {
                    pointsList.add(Point(pointXList[i], pointYList[i]))
                }
            }
        }
        val realPointsList = MutableList(pointsList.size) { pointsList[it] }
        realPointsList.removeAt(realPointsList.lastIndex)
        pointsFloatArray = FloatArray(realPointsList.size * 2) { i -> getFloatArrayElement(i, realPointsList)}

        //画线参数的点，需把中间的点都复制一个，以把线连起来
        val linePointsList: MutableList<Point> = arrayListOf()
        for (index in pointsList.indices) {
            if (index == 0 || index == pointsList.indices.last) {
                linePointsList.add(pointsList[index])
            } else {
                linePointsList.add(pointsList[index])
                linePointsList.add(pointsList[index])
            }
        }

        lineFloatArray = FloatArray(linePointsList.size * 2) { i -> getFloatArrayElement(i, linePointsList)}

        invalidate()
    }

    fun clearData() {
        mRankList?.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (originLayer == null) {
            originLayer = canvas.save()
        }
        //还原画布到初始状态
        canvas.restoreToCount(originLayer!!)

        val rankList = mRankList
        if (!rankList.isNullOrEmpty()) {
            canvas.save()
            //画线
            canvas.drawLines(lineFloatArray, linePaint)
            //画小圆点
            canvas.drawPoints(pointsFloatArray, pointPaint)
            //画大圆
            bigPointPaint.style = Paint.Style.FILL
            bigPointPaint.color = Color.parseColor("#6c9fff")
            canvas.drawCircle(pointsList.last().x.toFloat(), pointsList.last().y.toFloat(),
                    bigPointRadius, bigPointPaint)
            bigPointPaint.style = Paint.Style.STROKE
            bigPointPaint.color = Color.WHITE
            bigPointPaint.strokeWidth = bigPointStrokeWidth
            canvas.drawCircle(pointsList.last().x.toFloat(), pointsList.last().y.toFloat(),
                    bigPointRadius + bigPointStrokeWidth, bigPointPaint)
            bigPointPaint.color = Color.parseColor("#6c9fff")
            canvas.drawCircle(pointsList.last().x.toFloat(), pointsList.last().y.toFloat(),
                    bigPointRadius + bigPointStrokeWidth * 2, bigPointPaint)
            //文字绘制
            for (index in rankList.indices) {
                val rank = "第${rankList[index]}名"
                val textWidth = textPaint.measureText(rank)
                val textX = pointsList[index].x - textWidth / 2
                //判断圆点是否在中点之上，如果是就把文字画在圆点下方，反之上方
                val midHeight = paddingTop + maxHeight / 2
                val pointRadius = if (index == rankList.indices.last) bigPointRadius else smallPointsRadius
                val textDescent = textPaint.fontMetrics.descent
                val textAscent = textPaint.fontMetrics.ascent
                val textY = if (pointsList[index].y > midHeight) {
                    pointsList[index].y - pointRadius - textMargin - textDescent
                } else {
                    pointsList[index].y + pointRadius + textMargin - textAscent
                }
                //画文字背景
                textPaint.color = Color.parseColor("#9babc9")
                textBgRectF.set(textX - textPaddingHorizontal, textY + textAscent - textPaddingVertical,
                        textX + textWidth + textPaddingHorizontal, textY + textDescent + textPaddingVertical)
                canvas.drawRoundRect(textBgRectF, textBgRectRadius, textBgRectRadius, textPaint)
                textPaint.color = Color.WHITE
                //画文字
                canvas.drawText(rank, textX, textY, textPaint)

            }
            canvas.restore()
        }

    }


    private fun getFloatArrayElement(i : Int, list: MutableList<Point>): Float{
        val index = (i + if (i % 2 == 0) 2 else 1) / 2 - 1
        return if (i % 2 == 0) list[index].x.toFloat() else list[index].y.toFloat()
    }

}