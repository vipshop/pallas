<template>
    <div>
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
        },
        grid: {
          top: '40',
          left: '3%',
          right: '3%',
          bottom: '3%',
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
        // const itemStyle = {
        //   markPoint: {
        //     data: [
        //       { name: '最大值', type: 'max' },
        //     ],
        //   },
        // };
        this.$set(rObj, 'type', 'line');
        // this.$set(rObj, 'itemStyle', itemStyle);
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
    // this.drawLine();
  },
};
</script>

<style>
</style>
