<template>
    <div>
        <div :id="id" style="width: 100%;height:200px"></div>
    </div>
</template>

<script>
import echarts from 'echarts';
import moment from 'moment';

require('echarts/lib/chart/bar');

export default {
  props: ['id', 'optionInfo'],
  data() {
    return {
      myChart: {},
      option: {
        color: ['#13CE66', '#20A0FF', '#F7BA2A', '#FF4949'],
        backgroundColor: '#373a3c',
        tooltip: {
          trigger: 'axis',
          formatter(params) {
            let result = `<b>${moment(Number(params[0].name)).format('YYYY-MM-DD HH:mm:ss')}</b><br/>`;
            params.forEach((ele) => {
              result += `${ele.seriesName}: ${ele.value}<br/>`;
            });
            return result;
          },
        },
        title: {
          text: null,
        },
        grid: {
          top: '35',
          left: '1%',
          right: '2%',
          bottom: '25',
          containLabel: true,
        },
        legend: {
          y: 'bottom',
          padding: [
            5,  // 上
            10, // 右
            5,  // 下
            10, // 左
          ],
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: [],
          axisLabel: {
            formatter(value) {
              return `${moment(Number(value)).format('HH:mm')}\n${moment(Number(value)).format('MM-DD')}`;
            },
          },
        },
        yAxis: {
          type: 'value',
          name: '',
          splitLine: {
            show: true,
            lineStyle: {
              type: 'solid',
              color: ['#444444'],
            },
          },
        },
        series: [],
      },
    };
  },
  watch: {
    optionInfo: {
      handler() {
        this.drawLine();
      },
      deep: true,
    },
  },
  methods: {
    resize() {
      window.addEventListener('resize', () => {
        this.myChart.resize();
      });
    },
    drawLine() {
      const seriesArray = this.optionInfo.seriesData.map((obj) => {
        const rObj = { ...obj };
        const label = {
          normal: {
            show: true,
            position: 'top',
          },
        };
        this.$set(rObj, 'type', 'bar');
        this.$set(rObj, 'label', label);
        return rObj;
      });
      this.option.xAxis.data = this.optionInfo.xCategories;
      this.option.yAxis.name = this.optionInfo.yTitle;
      this.option.series = seriesArray;
      this.myChart = echarts.init(document.getElementById(this.id), 'dark');
      this.myChart.setOption(this.option);
      this.resize();
    },
  },
  mounted() {
    // this.drawLine();
  },
};
</script>
