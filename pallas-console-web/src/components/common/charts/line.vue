<template>
    <div style="height:300px">
        <chart-no-data v-if="JSON.stringify(optionInfo) === '{}'"></chart-no-data>
        <div :id="id" style="width: 100%;height:300px"></div>
    </div>
</template>

<script>
import echarts from 'echarts';
import moment from 'moment';

require('echarts/lib/chart/line');

export default {
  props: ['id', 'optionInfo'],
  data() {
    return {
      myChart: {},
      option: {
        color: ['#13CE66', '#20A0FF', '#F7BA2A', '#FF4949'],
        backgroundColor: '#373a3c',
        title: {
          text: null,
        },
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
        legend: {
          y: 'bottom',
        },
        grid: {
          top: '45',
          left: '3%',
          right: '3%',
          bottom: '25',
          containLabel: true,
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
        const markPoint = {
          data: [
            { type: 'max', name: '最大值' },
            { type: 'min', name: '最小值' },
          ],
        };
        this.$set(rObj, 'type', 'line');
        this.$set(rObj, 'symbol', 'none');
        this.$set(rObj, 'smooth', false);
        this.$set(rObj, 'markPoint', markPoint);
        return rObj;
      });
      this.option.xAxis.data = this.optionInfo.xAxis;
      this.option.yAxis.name = this.optionInfo.yAxisName;
      this.option.series = seriesArray;
      this.myChart = echarts.init(document.getElementById(this.id), 'dark');
      this.myChart.setOption(this.option);
      this.resize();
    },
  },
  mounted() {
    this.drawLine();
  },
};
</script>

<style>
</style>
