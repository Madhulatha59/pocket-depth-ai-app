/* ============================================================
   analytics.js — Clinical Periodontal Engine
   Port of ToothViewModel's analyzeAndStoreMetrics logic
   ============================================================ */

const AnalyticsEngine = (() => {
  /**
   * Computes clinical metrics and classification based on tooth measurements
   * @param {Object} state - The current application state containing manual inputs and allToothData
   * @returns {Object} Updated metrics & classifications
   */
  function computeMetrics(state) {
    const ppdValues = [];
    const calValues = [];
    
    const manualPpd = state.manualPpd || {};
    const manualCal = state.manualCal || {};
    const manualBop = state.manualBop || {};
    const allToothData = state.allToothData || [];

    // 1. Gather PPD and CAL values for each tooth (1 to 32)
    for (let toothNum = 1; toothNum <= 32; toothNum++) {
      // PPD values (3 sites per tooth: index 0, 1, 2)
      const manualValsPpd = [];
      for (let index = 0; index <= 2; index++) {
        const val = manualPpd[`tooth${toothNum}_ppd_${index}`];
        if (val !== undefined && val !== null && val !== '') {
          const parsed = parseInt(val, 10);
          if (!isNaN(parsed)) manualValsPpd.push(parsed);
        }
      }

      if (manualValsPpd.length > 0) {
        ppdValues.push(...manualValsPpd);
      } else {
        // Fallback to voice measurements
        const voiceEntries = allToothData.filter(t => t.toothNumber === toothNum);
        if (voiceEntries.length > 0) {
          // Get the latest timestamp entry
          voiceEntries.sort((a, b) => b.timestamp - a.timestamp);
          ppdValues.push(...voiceEntries[0].values);
        }
      }

      // CAL values (3 sites per tooth: index 0, 1, 2)
      const manualValsCal = [];
      for (let index = 0; index <= 2; index++) {
        const val = manualCal[`tooth${toothNum}_cal_${index}`];
        if (val !== undefined && val !== null && val !== '') {
          const parsed = parseInt(val, 10);
          if (!isNaN(parsed)) manualValsCal.push(parsed);
        }
      }
      if (manualValsCal.length > 0) {
        calValues.push(...manualValsCal);
      }
    }

    // 2. Count Bleeding on Probing (BOP)
    let bopCount = 0;
    const measuredTeeth = Object.keys(manualBop).length;
    for (let key in manualBop) {
      if (manualBop[key] === true) {
        bopCount++;
      }
    }

    // 3. Calculate Mean PPD
    const avgPpd = ppdValues.length > 0 ? ppdValues.reduce((a, b) => a + b, 0) / ppdValues.length : 2.0;
    const meanPpd = Math.round(avgPpd * 10) / 10;

    // 4. Calculate Mean CAL (estimate conservatively from PPD if empty)
    let avgCal = 0.0;
    if (calValues.length > 0) {
      avgCal = calValues.reduce((a, b) => a + b, 0) / calValues.length;
    } else {
      if (ppdValues.length > 0) {
        avgCal = Math.max(0, avgPpd - 1.5);
      } else {
        avgCal = 0.5;
      }
    }
    const meanCal = Math.round(avgCal * 10) / 10;

    // 5. BOP Percentage
    let bopPercentage = 10.0;
    if (measuredTeeth > 0) {
      bopPercentage = (bopCount / measuredTeeth) * 100.0;
    } else if (ppdValues.length > 0) {
      // Estimate BOP conservatively from proportion of deep pockets (>=4mm)
      const deepPocketTeeth = ppdValues.filter(v => v >= 4).length;
      const estimatedBop = (deepPocketTeeth / ppdValues.length) * 60.0;
      bopPercentage = Math.min(100, Math.max(0, estimatedBop));
    }
    bopPercentage = Math.round(bopPercentage);

    // 6. Deep pockets count (sites with PPD >= 4mm or CAL >= 4mm)
    const deepPocketsCount = (ppdValues.length > 0 || calValues.length > 0)
      ? ppdValues.filter(v => v >= 4).length + calValues.filter(v => v >= 4).length
      : 0;

    // 7. AAP/EFP 2018 Guidelines Classification Fallback
    const localClass = (() => {
      if (avgPpd >= 5.0 || avgCal >= 4.0 || bopPercentage > 60 || deepPocketsCount > 15) return 3; // Severe
      if (avgPpd >= 3.8 || avgCal >= 2.8 || bopPercentage > 30 || deepPocketsCount > 5)  return 2; // Moderate
      if (avgPpd >= 2.8 || avgCal >= 1.2 || bopPercentage > 20 || deepPocketsCount > 1)  return 1; // Mild
      return 0; // Healthy
    })();

    let clinicalVerdict = "Healthy Periodontium";
    let stageAndGrade = "Healthy, Grade A";
    let probHealthy = 90;
    let probModerate = 8;
    let probSevere = 2;

    switch (localClass) {
      case 3:
        clinicalVerdict = "Severe Periodontitis";
        stageAndGrade = "Stage III, Grade C";
        probSevere = 82;
        probModerate = 14;
        probHealthy = 4;
        break;
      case 2:
        clinicalVerdict = "Moderate Periodontitis";
        stageAndGrade = "Stage II, Grade B";
        probModerate = 78;
        probSevere = 14;
        probHealthy = 8;
        break;
      case 1:
        clinicalVerdict = "Mild Periodontitis";
        stageAndGrade = "Stage I, Grade A";
        probHealthy = 58;
        probModerate = 30;
        probSevere = 12;
        break;
      default:
        clinicalVerdict = "Healthy Periodontium";
        stageAndGrade = "Healthy, Grade A";
        probHealthy = 90;
        probModerate = 8;
        probSevere = 2;
        break;
    }

    return {
      meanPpd,
      meanCal,
      bopPercentage,
      deepPocketsCount,
      clinicalVerdict,
      stageAndGrade,
      probHealthy,
      probModerate,
      probSevere
    };
  }

  return { computeMetrics };
})();
