import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

interface DatasetInfo {
  label: string;
  color: string;
  data: number[];
  visible: boolean;
}

@Component({
  selector: 'app-balance-chart',
  templateUrl: './balance-chart.html',
  styleUrl: './balance-chart.scss'
})
export class BalanceChart implements AfterViewInit {
  @ViewChild('balanceCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  chart!: Chart;

  datasets: DatasetInfo[] = [
    { label: 'SCB', color: '#5B428F', data: [40000, 42000, 39000, 45000, 43000, 45000], visible: true },
    { label: 'LHB', color: '#E58E58', data: [2000, 2500, 2500, 3000, 2800, 3000], visible: true },
    { label: 'Kept', color: '#5D9C96', data: [75000, 75000, 78000, 78000, 80000, 80000], visible: true },
    { label: 'DIME', color: '#D96B6B', data: [10000, 10500, 11000, 11500, 12000, 12000], visible: true },
    { label: 'K Mobile', color: '#6B8E7B', data: [6000, 5500, 4800, 5000, 5800, 5200], visible: true }
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
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
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
