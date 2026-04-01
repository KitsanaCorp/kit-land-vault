import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

interface DatasetInfo {
  label: string;
  color: string;
  data: number[];
  visible: boolean;
  baseBalance: number;
}

@Component({
  selector: 'app-balance-chart',
  imports: [CommonModule],
  templateUrl: './balance-chart.html',
  styleUrl: './balance-chart.scss'
})
export class BalanceChart implements AfterViewInit {
  @ViewChild('balanceCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  chart!: Chart;

  periods = ['1w', '1m', 'ytd', '1y', '5y', 'max'];
  activePeriod = '1w';

  datasets: DatasetInfo[] = [
    { label: 'SCB', color: '#5B428F', data: [], visible: true, baseBalance: 40000 },
    { label: 'LHB', color: '#E58E58', data: [], visible: true, baseBalance: 2500 },
    { label: 'Kept', color: '#5D9C96', data: [], visible: true, baseBalance: 75000 },
    { label: 'DIME', color: '#D96B6B', data: [], visible: true, baseBalance: 11000 },
    { label: 'K Mobile', color: '#6B8E7B', data: [], visible: true, baseBalance: 5000 }
  ];

  ngAfterViewInit(): void {
    const ctx = this.canvasRef.nativeElement.getContext('2d')!;

    const chartDatasets = this.datasets.map(ds => ({
      label: ds.label,
      data: ds.data,
      borderColor: ds.color,
      backgroundColor: ds.color + '20',
      borderWidth: 3,
      tension: 0.4,
      fill: true,
      pointRadius: 0,
      pointHoverRadius: 6
    }));

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: chartDatasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            mode: 'index',
            intersect: false,
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            titleColor: '#334155',
            bodyColor: '#334155',
            borderColor: '#E2E8F0',
            borderWidth: 1,
            padding: 10,
            boxPadding: 4
          }
        },
        scales: {
          y: {
            display: true,
            grid: { color: '#F1F5F9' },
            ticks: { font: { family: 'Inter', size: 10 }, color: '#94A3B8' }
          },
          x: {
            grid: { display: false },
            ticks: { font: { family: 'Inter', size: 10 }, color: '#94A3B8' }
          }
        },
        interaction: { mode: 'nearest', axis: 'x', intersect: false }
      }
    });

    // Initialize the chart with 1w data
    this.selectPeriod('1w');
  }

  selectPeriod(period: string): void {
    this.activePeriod = period;
    let labels: string[] = [];
    let dataPointsCount = 0;

    switch (period) {
      case '1w':
        labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        dataPointsCount = 7;
        break;
      case '1m':
        labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4'];
        dataPointsCount = 4;
        break;
      case 'ytd':
        labels = ['Jan', 'Feb', 'Mar', 'Apr']; // Mock current YTD up to April
        dataPointsCount = 4;
        break;
      case '1y':
        labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        dataPointsCount = 12;
        break;
      case '5y':
        labels = ['2022', '2023', '2024', '2025', '2026'];
        dataPointsCount = 5;
        break;
      case 'max':
        labels = ['2016', '2018', '2020', '2022', '2024', '2026'];
        dataPointsCount = 6;
        break;
    }

    // Update labels
    this.chart.data.labels = labels;

    // Generate specific mock data based on period
    this.chart.data.datasets.forEach((dataset, index) => {
      const baseBalance = this.datasets[index].baseBalance;
      const newData = [];
      let currentVal = baseBalance;
      
      for (let i = 0; i < dataPointsCount; i++) {
        // Mock random fluctuation
        const fluctuation = (Math.random() - 0.3) * (baseBalance * 0.05); // slight upward bias
        currentVal = Math.max(0, currentVal + fluctuation);
        newData.push(Math.round(currentVal));
      }

      dataset.data = newData;
      this.datasets[index].data = newData; // update internal model as well
    });

    this.chart.update();
  }

  toggleDataset(index: number): void {
    const ds = this.datasets[index];
    ds.visible = !ds.visible;
    if (ds.visible) {
      this.chart.show(index);
    } else {
      this.chart.hide(index);
    }
  }
}
